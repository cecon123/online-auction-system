# Git Workflow

This document outlines the branching and collaboration strategy for the Online Auction System project.

## 1. Branching Strategy

- **`main`**: The stable branch. Contains the latest demo-ready code. No direct commits allowed.
- **`dev`**: The integration branch. All feature branches are merged here first.
- **`feature/<task-name>-<developer-name>`**: Individual task branches. 

Example: `feature/sqlite-user-dao-manh`

## 2. Daily Workflow

### Step 1: Sync your local `dev`
Before starting a new task, ensure your integration branch is up to date.

```bash
git checkout dev
git pull origin dev
```

### Step 2: Create a feature branch
```bash
git checkout -b feature/my-new-task-huy
```

### Step 3: Work and Commit
Follow [Conventional Commits](https://www.conventionalcommits.org/):
- `feat:` for new features.
- `fix:` for bug fixes.
- `docs:` for documentation updates.
- `refactor:` for code restructuring.
- `test:` for adding/updating tests.

```bash
git add .
git commit -m "feat: add auction lock manager"
```

### Step 4: Merge into `dev`
Once the task is finished and tested locally:

1. Push your branch:
   ```bash
   git push -u origin feature/my-new-task-huy
   ```
2. Pull latest `dev` into your feature branch to resolve conflicts:
   ```bash
   git checkout dev
   git pull origin dev
   git checkout feature/my-new-task-huy
   git merge dev
   ```
3. Run tests one last time:
   ```bash
   mvn clean test
   ```
4. Merge into `dev`:
   ```bash
   git checkout dev
   git merge feature/my-new-task-huy
   git push origin dev
   ```

## 3. Pull Requests & Code Review

- For larger changes, create a Pull Request on GitHub from `feature/*` to `dev`.
- Tag the Lead (Huy) for review.
- After approval, the Lead will merge the branch.

## 4. Post-Merge Rule

Every time you pull from `dev`, it is **mandatory** to run:

```bash
mvn clean install
```

This ensures your local `target` folders and dependencies are synchronized across the multi-module project.
