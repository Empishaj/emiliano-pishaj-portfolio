# ADR-060 — Infrastructure as Code mit Terraform

| Feld       | Wert                                              |
|------------|---------------------------------------------------|
| Status     | ✅ Akzeptiert                                     |
| Java       | 21 · Terraform 1.7+ · AWS/GCP/Azure               |
| Datum      | 2024-01-01                                        |
| Kategorie  | DevOps / Infrastruktur                            |

---

## Kontext & Problem

Infrastruktur per Hand klicken ist nicht reproduzierbar, nicht versionierbar und nicht reviewbar. Ein vergessener Security-Group-Eintrag oder ein falsch konfigurierter S3-Bucket landet in der Zeitung. Infrastructure as Code (IaC) macht Infrastruktur zu Code: versioniert in Git, reviewbar als PR, automatisch applied durch CI/CD.

---

## Projektstruktur

```
infrastructure/
├── environments/
│   ├── staging/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── terraform.tfvars
│   └── production/
│       ├── main.tf
│       ├── variables.tf
│       └── terraform.tfvars
│
├── modules/                    ← Wiederverwendbare Module
│   ├── kubernetes-cluster/
│   │   ├── main.tf
│   │   ├── variables.tf
│   │   └── outputs.tf
│   ├── postgresql/
│   │   ├── main.tf
│   │   └── variables.tf
│   └── redis/
│       └── main.tf
│
└── .github/
    └── workflows/
        └── terraform.yml
```

---

## Terraform Modul: PostgreSQL

```hcl
# modules/postgresql/main.tf
variable "instance_name"    { type = string }
variable "environment"      { type = string }
variable "database_version" { type = string; default = "POSTGRES_16" }
variable "tier"             { type = string; default = "db-f1-micro" }
variable "region"           { type = string }

# Cloud SQL PostgreSQL Instanz (GCP Beispiel)
resource "google_sql_database_instance" "main" {
  name             = "${var.instance_name}-${var.environment}"
  database_version = var.database_version
  region           = var.region

  settings {
    tier = var.tier

    backup_configuration {
      enabled            = true
      start_time         = "02:00"  # 2 Uhr nachts
      retention_settings {
        retained_backups = 7  # 7 Tage Backups
      }
    }

    ip_configuration {
      ipv4_enabled    = false   # Kein öffentliches IP!
      private_network = var.vpc_network
    }

    maintenance_window {
      day  = 1   # Montag
      hour = 3   # 3 Uhr früh
    }

    insights_config {
      query_insights_enabled = true  # Query Performance Insights
    }
  }

  deletion_protection = var.environment == "production"  # Prod: Löschen verhindert
}

resource "google_sql_database" "app_db" {
  name     = "orderdb"
  instance = google_sql_database_instance.main.name
}

resource "google_sql_user" "app_user" {
  name     = "app"
  instance = google_sql_database_instance.main.name
  password = random_password.db_password.result
}

resource "random_password" "db_password" {
  length  = 32
  special = false
}

# Secret Manager: Passwort sicher speichern
resource "google_secret_manager_secret" "db_password" {
  secret_id = "${var.instance_name}-${var.environment}-db-password"
  replication { automatic {} }
}

resource "google_secret_manager_secret_version" "db_password" {
  secret      = google_secret_manager_secret.db_password.id
  secret_data = random_password.db_password.result
}

output "connection_name" { value = google_sql_database_instance.main.connection_name }
output "db_password_secret" { value = google_secret_manager_secret.db_password.name }
```

---

## Environment Konfiguration

```hcl
# environments/production/main.tf
terraform {
  required_version = ">= 1.7.0"

  # Remote State: Terraform State in GCS (nicht lokal!)
  backend "gcs" {
    bucket = "my-terraform-state"
    prefix = "production/order-service"
  }

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
  }
}

provider "google" {
  project = var.project_id
  region  = var.region
}

# PostgreSQL via Modul
module "postgresql" {
  source = "../../modules/postgresql"

  instance_name = "order-service"
  environment   = "production"
  tier          = "db-n1-standard-2"   # Prod: mehr Ressourcen
  region        = var.region
  vpc_network   = module.network.vpc_self_link
}

# GKE Cluster
module "kubernetes" {
  source = "../../modules/kubernetes-cluster"

  cluster_name = "order-service-prod"
  environment  = "production"
  node_count   = 3
  machine_type = "n2-standard-4"
}
```

---

## Terraform Best Practices

```hcl
# ① Niemals Secrets im Code — immer aus Secret Manager / Vault
# ❌ Falsch:
resource "google_sql_user" "app" {
  password = "mein-super-geheimes-pw"  # Im Git! Katastrophe!
}

# ✅ Richtig: random_password + Secret Manager (siehe oben)

# ② Alle Ressourcen mit Tags/Labels für Kosten-Tracking
resource "google_compute_instance" "app" {
  labels = {
    environment = var.environment
    team        = "backend"
    service     = "order-service"
    managed_by  = "terraform"
  }
}

# ③ Versionierte Module — kein :latest
module "postgresql" {
  source  = "hashicorp/google/postgresql"
  version = "~> 5.0"  # Pin auf Major-Version
}

# ④ Lifecycle-Regeln für sensible Ressourcen
resource "google_sql_database_instance" "main" {
  lifecycle {
    prevent_destroy = true  # Verhindert versehentliches Löschen
    ignore_changes  = [settings[0].maintenance_window]
  }
}

# ⑤ Output-Werte für andere Module/Services
output "db_connection_name" {
  description = "Cloud SQL connection name für App"
  value       = google_sql_database_instance.main.connection_name
  sensitive   = false
}

output "db_password_secret_name" {
  description = "Secret Manager Secret für DB-Passwort"
  value       = google_secret_manager_secret.db_password.name
  sensitive   = true   # Wird in Plan-Output maskiert
}
```

---

## CI/CD für Terraform

```yaml
# .github/workflows/terraform.yml
name: Terraform

on:
  pull_request:
    paths: ["infrastructure/**"]
  push:
    branches: [main]
    paths: ["infrastructure/**"]

jobs:
  terraform-plan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: hashicorp/setup-terraform@v3
        with:
          terraform_version: "1.7.0"

      - name: Authenticate GCP
        uses: google-github-actions/auth@v2
        with:
          credentials_json: ${{ secrets.GCP_CREDENTIALS }}

      - name: Terraform Init
        run: terraform -chdir=infrastructure/environments/production init

      - name: Terraform Validate
        run: terraform -chdir=infrastructure/environments/production validate

      - name: Terraform Plan
        run: |
          terraform -chdir=infrastructure/environments/production plan \
            -out=tfplan \
            -var-file=terraform.tfvars

      - name: Post Plan as PR Comment
        uses: borchero/terraform-plan-comment@v1
        with:
          plan-path: infrastructure/environments/production/tfplan

  terraform-apply:
    runs-on: ubuntu-latest
    needs: terraform-plan
    if: github.ref == 'refs/heads/main'
    environment: production  # Manual Approval Gate!
    steps:
      - name: Terraform Apply
        run: |
          terraform -chdir=infrastructure/environments/production apply \
            -auto-approve tfplan
```

---

## tfsec: Security-Scan für Terraform

```yaml
- name: tfsec Security Scan
  uses: aquasecurity/tfsec-action@v1.0.0
  with:
    working_directory: infrastructure/

# tfsec findet:
# - S3 Bucket ohne Verschlüsselung
# - Security Groups mit 0.0.0.0/0
# - IAM-Policies mit zu weiten Berechtigungen
# - PostgreSQL ohne Deletion Protection
```

---

## Konsequenzen

**Positiv:** Infrastruktur reviewbar via PR. Jede Änderung in Git-History nachvollziehbar. `terraform plan` zeigt exakt was sich ändert — keine Überraschungen. Staging und Production identisch (nur verschiedene tfvars).

**Negativ:** Terraform State ist ein Single Point of Truth — muss gesichert und gesperrt sein (remote backend). `terraform destroy` löscht echte Infrastruktur — `prevent_destroy` für Prod obligatorisch. Lernkurve für HCL-Syntax und Provider-APIs.

---

## 💡 Guru-Tipps

- **Terraform State Lock**: Remote Backend (GCS, S3) mit State Lock verhindert parallele Applies.
- **`terraform import`** für bestehende Ressourcen: vorhandene Cloud-Ressourcen in Terraform-Management aufnehmen.
- **Atlantis** als Alternative zu GitHub Actions: Terraform via PR-Comments (`atlantis plan`, `atlantis apply`).
- **OpenTofu**: Open-Source-Fork von Terraform — Drop-in-Replacement, kompatibel.

---

## Verwandte ADRs

- [ADR-036](ADR-036-devops-cicd.md) — CI/CD-Pipeline auch für IaC.
- [ADR-038](ADR-038-kubernetes.md) — Kubernetes-Cluster via Terraform provisioniert.
- [ADR-015](ADR-015-sicherheit-owasp.md) — tfsec als Security-Gate für Infrastruktur.
