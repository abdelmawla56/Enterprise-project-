# WorkHub SaaS Backend

## Architecture
The application uses a standard Spring Boot layered architecture: Controllers handle HTTP requests, Services contain business logic, Repositories interface with the H2 database via Spring Data JPA, and Entities model the relational schema mapped with Hibernate. DTOs are used for explicit API contracts and decoupling the presentation from the data layer. 

## Tenant Approach
A strict shared-database, shared-schema approach has been implemented using a discriminator column (`tenant_id`). The strategy uses Hibernate's `@FilterDef` on a `@MappedSuperclass` called `BaseMultiTenantEntity`. A JWT extraction interceptor populates a ThreadLocal `TenantContext`. A Spring AOP Aspect (`TenantFilterAspect`) hooks into all Repository methods, applying the Hibernate filter with the current `tenantId`. This guarantees Tenant A cannot read Tenant B data, fulfilling isolation constraints seamlessly across all entities extending the base class.

## Transaction Boundary
Transactions are bound at the Service layer using `@Transactional`. For the mandatory rollback demonstration, creating a project with embedded tasks via the `ProjectService.createProjectWithTasks` checks for a task title matching an intentionally failed identifier. A runtime exception triggers the default rollback mechanism, preventing the project from persisting without its failed task, enforcing the atomicity constraint.
