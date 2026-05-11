# WorkHub Infrastructure as Code (Terraform)

## Why Infrastructure as Code (IaC)?
IaC transforms manual infrastructure management into a software engineering practice.
1. **Prevents Infrastructure Drift**: By defining everything in code, we ensure the actual state matches the desired state. Manual changes in the console are detected and corrected by `terraform apply`.
2. **Immutability**: We treat servers and networking as disposable. If we need a change, we update the code and redeploy.
3. **Collaboration**: Infrastructure is version-controlled, allowing for peer reviews and audits.

## Best Practices
- **Remote State**: Always use a remote backend (S3/GCS) with state locking (DynamoDB) to prevent concurrent modifications.
- **Module Usage**: Use verified modules (like `terraform-aws-modules`) to follow community-standard architectures.
- **Least Privilege**: Terraform should run with a service account that has only the permissions required to manage the specific resources.
- **Environment Separation**: Use workspaces or separate state files for `dev`, `staging`, and `prod`.
