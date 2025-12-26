# TODO — DDS + Payment Switch Access Token Services

<!-- This TODO list complements README.md and captures the next implementation steps. -->

## Service split

- [x] Decide final service names and folders:
  - **DDS**: `getaccesstoken/dds/`
  - **Payment Switch**: `getaccesstoken/paymentSwitch/`
- [x] Rename or copy the legacy `getaccesstoken/` implementation into the **DDS** service folder.
- [x] Create a **Payment Switch** access-token service directory with its own `service-def.json`
  (mirroring the DDS layout but with card-focused validation).
- [x] Update any internal references that still point to the legacy `getaccesstoken/` path.
- [x] Ensure both services keep **shared library usage** aligned (`AtomicQuery`, `utils/`, `enums/`).

## DDS access-token flow

- [x] Wire request validation for `accounts` and `identity` fields.
- [ ] Document DDS-specific configuration (routing/account constraints, allowed account types).
- [x] Add DDS-focused sample payloads under the DDS service folder.

## Payment Switch access-token flow

- [ ] Wire request validation for `cards` and `identity` fields.
- [ ] Add Payment Switch sample payloads under the Payment Switch service folder.
- [ ] Document Payment Switch-specific configuration (card brand, expiry format, etc.).

## Cross-service consistency

- [ ] Share common request parsing helpers to reduce duplication.
- [ ] Align logging/metrics names so DDS and Payment Switch can be compared side-by-side.
- [ ] Update any integration docs that still reference the legacy generic service name.
