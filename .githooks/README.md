# Git Hooks

This directory contains Git hooks for the expense-segmentation project.

## Available Hooks

### pre-push
Runs before `git push` to ensure code quality:
1. **Spotless**: Applies code formatting (auto-fixes and stages changes)
2. **Tests**: Runs all unit tests
3. **Coverage**: Verifies code coverage meets minimum requirements

**Note**: Only runs when backend files have changed.

## Installation

Run the installation script from the project root:

```bash
./.githooks/install-hooks.sh
```

## Manual Installation

If you prefer to install manually:

```bash
# From project root
ln -s ../../.githooks/pre-push .git/hooks/pre-push
chmod +x .git/hooks/pre-push
```

## Skipping Hooks

If you need to skip the pre-push hook (use sparingly):

```bash
git push --no-verify
```

## Uninstalling

```bash
rm .git/hooks/pre-push
```

## Troubleshooting

**Hook fails on spotless:**
- Run `./gradlew spotlessApply` manually in the backend directory
- Commit the formatted files

**Hook fails on tests:**
- Run `./gradlew test` in the backend directory to see detailed errors
- Fix failing tests before pushing

**Hook fails on coverage:**
- Run `./gradlew jacocoTestReport` to see coverage report
- Add tests to meet minimum coverage requirements
