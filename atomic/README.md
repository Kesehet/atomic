# Atomic Access Token Services (DDS + Payment Switch)

<!-- This README documents the two dedicated access-token services and the intent of the repo. -->

## Overview

<!-- Explain the objective at a high level without reusing the old generic service name. -->
This repository provides **two dedicated access-token services** that power Atomic switch flows:

1. **DDS (Direct Deposit Switch) Access Token Service**
2. **Payment Switch Access Token Service**

Both services are intentionally separated to keep switch-specific behavior, validation rules, and
observability distinct while **sharing the same underlying libraries** (for example, `AtomicQuery`
and shared utilities under `utils/`).

## Why two services instead of one

<!-- Describe the split and its benefits for maintainability and compliance. -->
Splitting the access-token logic into two services ensures:

- **Clear ownership of switch-specific configuration** (DDS vs Payment Switch).
- **Separate request validation** and eligibility checks tailored to each switch.
- **Independent monitoring and troubleshooting** in production workflows.
- **Reduced risk of cross-switch regressions** when iterating on one flow.

## Service responsibilities

<!-- Detail the responsibilities of each service with explicit boundaries. -->
### DDS Access Token Service

- Handles **Direct Deposit Switch** onboarding flows.
- Accepts identifiers and metadata needed to initiate a DDS session.
- Uses **AtomicQuery** and shared utilities to fetch/validate prerequisite data.
- Produces an access token scoped only to DDS workflows.

### Payment Switch Access Token Service

- Handles **Payment Switch** onboarding flows.
- Accepts identifiers and metadata tailored to payment-switch use cases.
- Uses the **same shared libraries** (e.g., `AtomicQuery`) for consistency.
- Produces an access token scoped only to Payment Switch workflows.

## Sample access-token requests

<!-- Provide concrete examples and point to docs for exact field requirements. -->
> **Note**: These examples mirror the structure shown in Atomic’s switch documentation.
> Always follow the latest schema in the official docs:
> <https://docs.atomicfi.com/products/switch/implementation>

### DDS access-token request (example)

<!-- Example request body for DDS (bank account focus). -->
```json
{
  "identifier": "YOUR_INTERNAL_IDENTIFIER",
  "accounts": [
    {
      "accountNumber": "220000000",
      "routingNumber": "110000000",
      "type": "checking",
      "title": "Premier Plus Checking"
    }
  ],
  "identity": {
    "firstName": "Jane",
    "lastName": "Doe",
    "postalCode": "12345",
    "address": "123 Lane St",
    "address2": "Apt 987",
    "city": "Provo",
    "state": "UT",
    "phone": "8011234576",
    "email": "jane@example.com"
  }
}
```

### Payment Switch access-token request (example)

<!-- Example request body for Payment Switch (card focus). -->
```json
{
  "identifier": "YOUR_INTERNAL_IDENTIFIER",
  "cards": [
    {
      "title": "Premium Card",
      "brand": "mastercard",
      "number": "4242424242421111",
      "expiry": "12/29",
      "cvv": "887"
    }
  ],
  "identity": {
    "firstName": "Jane",
    "lastName": "Doe",
    "postalCode": "12345",
    "address": "123 Lane St",
    "address2": "Apt 987",
    "city": "Provo",
    "state": "UT",
    "phone": "8011234576",
    "email": "jane@example.com"
  }
}
```

### Payment Switch configuration notes

<!-- Keep card-specific configuration aligned with Atomic switch docs. -->
- **Card brand**: Provide a supported brand label (for example, `mastercard` or `visa`).
- **Expiry format**: Use `MM/YY` (e.g., `12/29`) in the `cards[].expiry` field.
- **Card security**: Include `cards[].cvv` for validation with the switch provider.

### Key differences between DDS and Payment Switch requests

<!-- Highlight request differences while keeping links to the official docs. -->
- **Funding instrument**: DDS requests include **bank account** details (`accounts` with routing
  and account numbers), while Payment Switch requests include **card** details (`cards` with
  card number and expiry). (See:
  <https://docs.atomicfi.com/products/switch/implementation>)
- **Shared identity**: Both request types include an `identity` object with consumer contact
  details, matching the structure shown in the official documentation. (See:
  <https://docs.atomicfi.com/products/switch/implementation>)

## Shared building blocks

<!-- Highlight shared libraries to avoid duplication across the two services. -->
Both services rely on:

- **AtomicQuery** for backend data retrieval and validation
- **Shared utility helpers** in `utils/`
- **Common enums/constants** under `enums/`
- **Matched service definitions** so both services include the same shared libraries
- **Integration patterns** used by the service directories (e.g., `getaccesstoken/dds/`)

## Repository layout (relevant paths)

<!-- Provide navigation hints for maintainers. -->
- `getaccesstoken/dds/` — DDS access-token implementation and artifacts (service definition, samples)
- `getaccesstoken/paymentSwitch/` — Payment Switch access-token implementation and artifacts
- `utils/` — shared helper functions
- `enums/` — shared constants
- `api/` — shared API and integration plumbing

## Implementation notes

<!-- Capture expectations for future updates and constraints. -->
- Each service should expose its **own service definition** and runtime configuration.
- **No shared endpoint** should issue tokens for both DDS and Payment Switch.
- Keep the services aligned with Atomic’s switch implementation guidance:
  <https://docs.atomicfi.com/products/switch/implementation>

## Next steps

<!-- Keep guidance explicit for someone implementing the split. -->
1. Create a **DDS-specific** access-token service folder (if the legacy folder is renamed).
2. Create a **Payment Switch** access-token service folder with its own service definition.
3. Ensure both services use the **same shared libraries** for query and validation.
4. Document any switch-specific configuration in each service folder’s docs.
