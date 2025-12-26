# Reference Notes

This document captures quick summaries of related classes referenced by the access-token work but
not implemented in this repository.

## `TxnProxy`

- Wrapper around a transaction object that exposes convenience accessors.
- Provides `formXml` and `appDoc` accessors (the latter returns a parsed XML `Document` for the form).
- Exposes transaction property helpers (for example, reading and writing txn properties used by
  service flows).

## `ViewCustomer`

- View model for a single customer (extends the base customer model in the TM/TAF stack).
- Contains identity/contact fields such as `firstName`, `lastName`, `email`, and `phoneNumber`.
- Provides address fields via `addressCurrent`, `addressMailing`, and `addressPrevious`, each of
  which exposes standard address fields like `street1`, `street2`, `city`, `state`, and `postalCode`.
- Supports restoring customer records from a transaction/app document.

## `ViewCustomers`

- Collection wrapper for `ViewCustomer` instances.
- Provides helpers like `restoreViewCustomersFromXml(Document)` to hydrate customers from a
  transaction/app document.
- Exposes a `primary` customer accessor for the primary applicant record.

## Address model (referenced by `ViewCustomer`)

- Represents a physical address associated with a customer (current, mailing, previous).
- Common fields include `street1`, `street2`, `city`, `state`, and `postalCode`.
