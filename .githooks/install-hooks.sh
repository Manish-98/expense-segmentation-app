#!/bin/bash
# Install Git hooks for the expense-segmentation project

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GIT_DIR="$(git rev-parse --git-dir)"

echo "📦 Installing Git hooks..."

# Make hooks executable
chmod +x "$SCRIPT_DIR/pre-push"

# Link or copy the pre-push hook
if [ -f "$GIT_DIR/hooks/pre-push" ]; then
    echo "⚠️  Pre-push hook already exists, backing up..."
    mv "$GIT_DIR/hooks/pre-push" "$GIT_DIR/hooks/pre-push.backup"
fi

ln -s "$SCRIPT_DIR/pre-push" "$GIT_DIR/hooks/pre-push"

echo "✅ Git hooks installed successfully!"
echo ""
echo "The following hooks are now active:"
echo "  • pre-push: Runs spotless, tests, and coverage verification"
echo ""
echo "To uninstall, run: rm $GIT_DIR/hooks/pre-push"
