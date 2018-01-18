# Demo Data for OpenLMIS Fulfillment Service
This folder holds demo data for the fulfillment service. The demo data is used by developers, QA
staff, and is automatically loaded into some environments for demo and testing purposes. It is not
for use in production environments.

Each .json file contains demo data that corresponds to one database table.

## Orders

The orders listed below are defined in the following files:
fulfillment.orders.json and fulfillment.order_line_items.json.

1. ORDER-00000000-0000-0000-0000-000000000006R
  * program: Family Planning
  * period: Jan2016
  * facility: FAC003 (inactive facility)
  * requesting facility: FAC003 (inactive facility)
  * receiving facility: FAC003 (inactive facility)
  * supplying facility: HC01/Comfort Health Clinic
  * status: RECEIVED
2. ORDER-00000000-0000-0000-0000-000000000007R
  * program: Essential Meds
  * period: Q1
  * facility: Balaka District Hospital
  * requesting facility: Balaka District Hospital
  * receiving facility: Balaka District Hospital
  * supplying facility: HC01/Comfort Health Clinic
  * status: IN_ROUTE
3. ORDER-00000000-0000-0000-0000-000000000008R
  * program: Family Planning
  * period: Feb2016
  * facility: FAC003 (inactive facility)
  * requesting facility: FAC003 (inactive facility)
  * receiving facility: FAC003 (inactive facility)
  * supplying facility: HC01/Comfort Health Clinic
  * status: READY_TO_PACK
4. ORDER-00000000-0000-0000-0000-000000000009R
  * program: Essential Meds
  * period: Q2
  * facility: Balaka District Hospita
  * requesting facility: Balaka District Hospital
  * receiving facility: Balaka District Hospital
  * supplying facility: HC01/Comfort Health Clinic
  * status: IN_ROUTE

The orders also contain line items and other fields not listed here (see the JSON files for the
complete data).

## Proof of Delivery

The PoDs listed below are defined in the following files:
fulfillment.proof_of_deliveries.json and fulfillment.proof_of_delivery_line_items.json.

1. PoD for ORDER-00000000-0000-0000-0000-000000000006R
  * it has 3 line items for quantities received

Facilities, Programs, Products, Requisition Groups and User Roles & Rights come from the
[Reference Data demo data](https://github.com/OpenLMIS/openlmis-referencedata/tree/master/demo-data).
