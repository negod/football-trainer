#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'USAGE'
Usage:
  scripts/bootstrap-template.sh \
    --project-name "Customer Portal" \
    --project-slug customer-portal \
    --java-package se.backede.customerportal \
    --db-name customer-portal
USAGE
}

project_name=""
project_slug=""
java_package=""
db_name=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --project-name)
      project_name="${2:-}"
      shift 2
      ;;
    --project-slug)
      project_slug="${2:-}"
      shift 2
      ;;
    --java-package)
      java_package="${2:-}"
      shift 2
      ;;
    --db-name)
      db_name="${2:-}"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1
      ;;
  esac
done

if [[ -z "$project_name" || -z "$project_slug" || -z "$java_package" || -z "$db_name" ]]; then
  usage
  exit 1
fi

java_package_path="${java_package//.//}"

if [[ ! -d backend/src/main/java/_template ]]; then
  echo "Expected backend/src/main/java/_template to exist. Run this from the repository root." >&2
  exit 1
fi

mkdir -p "backend/src/main/java/$java_package_path"
cp -R backend/src/main/java/_template/. "backend/src/main/java/$java_package_path/"
rm -rf backend/src/main/java/_template

while IFS= read -r file; do
  perl -pi \
    -e "s#__PROJECT_NAME__#${project_name}#g;" \
    -e "s#__PROJECT_SLUG__#${project_slug}#g;" \
    -e "s#__JAVA_PACKAGE__#${java_package}#g;" \
    -e "s#__JAVA_PACKAGE_PATH__#${java_package_path}#g;" \
    -e "s#__DB_NAME__#${db_name}#g;" \
    -e "s#package _template#package ${java_package}#g;" \
    -e "s#import _template#import ${java_package}#g;" \
    "$file"
done < <(find . -type f \
  -not -path './.git/*' \
  -not -path './frontend/node_modules/*' \
  -not -path './frontend/dist/*' \
  -not -path './frontend/playwright-report/*' \
  -not -path './frontend/test-results/*' \
  -not -path './backend/target/*' \
  -not -path './mission-control/.state/*' \
  -not -path './mission-control/node_modules/*')

echo "Template bootstrapped for $project_name."
echo "Next: npm install --prefix frontend && npm run build:backend && npm run build:frontend"
