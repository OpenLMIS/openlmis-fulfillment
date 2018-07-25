7.0.1 / WIP
==================

Improvements:
* [OLMIS-4646](https://openlmis.atlassian.net/browse/OLMIS-4646): Added Jenkinsfile
* [OLMIS-2923](https://openlmis.atlassian.net/browse/OLMIS-2923): Updated demo data loading approach
* [OLMIS-4354](https://openlmis.atlassian.net/browse/OLMIS-4354): Improved POST /api/shipments endpoint
  * Reduced number of calls to the stock management service
* [OLMIS-4293](https://openlmis.atlassian.net/browse/OLMIS-4293): Improved performance of get orders
* [OLMIS-4905](https://openlmis.atlassian.net/browse/OLMIS-4905): Updated notification service to use v2 endpoint.
* [OLMIS-4491](https://openlmis.atlassian.net/browse/OLMIS-4491): Added new demo data loading strategy.
* [OLMIS-4820](https://openlmis.atlassian.net/browse/OLMIS-4820): Updated BasicOrderDtoBuilder to fetch programs and periods in bulk.

7.0.0 / 2018-04-24
==================

Contract breaking changes:
* [OLMIS-3613](https://openlmis.atlassian.net/browse/OLMIS-3613): Removed approved quantity from Order Line Item
* [OLMIS-3956](https://openlmis.atlassian.net/browse/OLMIS-3956): Modified Proof Of Delivery model
  * Proof Of Delivery is related with shipment, not order
  * Proof Of Delivery and related line items have new fields
  * For external orders the service will create related shipment and Proof Of Delivery
  * Database migration will handle a new structure for legacy data
  * The submit endpoint has been merged with the update endpoint
  * The GET all POD endpoint will filter out those PODs to which user has no right.
* [OLMIS-3958](https://openlmis.atlassian.net/browse/OLMIS-3958): Ordered Quantity value is now in packs. Filled Quantity and Packs To Ship have been removed.
* [OLMIS-4165](https://openlmis.atlassian.net/browse/OLMIS-4165): Changed Order search endpoint and renamed its parameters, added database pagination and introduced permission strings.
* [OLMIS-4119](https://openlmis.atlassian.net/browse/OLMIS-4119): Change POD endpoint name to proofsOfDelivery.
* [OLMIS-4001](https://openlmis.atlassian.net/browse/OLMIS-4001): Made it possible to print Proof of Delivery. Removed the obsolete "/proofOfDeliveryTemplates" endpoint.

New features:
* [OLMIS-3663](https://openlmis.atlassian.net/browse/OLMIS-3663): Created Shipment resource model
* [OLMIS-1611](https://openlmis.atlassian.net/browse/OLMIS-1611): Send stock event when shipment is finalized
* [OLMIS-3881](https://openlmis.atlassian.net/browse/OLMIS-3881): Create Proof Of Delivery draft for finalized shipment
* [OLMIS-235](https://openlmis.atlassian.net/browse/OLMIS-235): Send Stock Event on Proof of Delivery confirmation
* [OLMIS-3991](https://openlmis.atlassian.net/browse/OLMIS-3991): Confirming Proof of Delivery will now result in sending a notification to the user that created the shipment
* [OLMIS-3994](https://openlmis.atlassian.net/browse/OLMIS-3994): Added audit logging for ProofOfDelivery
  * We are able to track:
    * Any changes to PoD line items
    * Any changes to receivedBy, deliveredBy, receivedDate fields
    * Confirmation of the PoD (changing status to confirmed)
  * The audit include changes made, date and user making changes.
  * The audit logs for any given PoD can be retrieve by related endpoint
* [OLMIS-4266](https://openlmis.atlassian.net/browse/OLMIS-4266): Provided expand pattern for shipments and PoDs
* [OLMIS-4262](https://openlmis.atlassian.net/browse/OLMIS-4262): Added orderId parameter to GET /proofsOfDelivery

Bug fixes and improvements added in a backwards-compatible manner:
* [OLMIS-3607](https://openlmis.atlassian.net/browse/OLMIS-3607): Added update details to Order
* [OLMIS-3608](https://openlmis.atlassian.net/browse/OLMIS-3608): Added possibility to extend last updater object in Order
* [OLMIS-3135](https://openlmis.atlassian.net/browse/OLMIS-3135): Handle API Key requests.
  * For now all requests are blocked.
* [OLMIS-3778](https://openlmis.atlassian.net/browse/OLMIS-3778): Fixed service checks the rights of a wrong user
* [OLMIS-3955](https://openlmis.atlassian.net/browse/OLMIS-3955): Renamed PICKING order status to FULFILLING. Removed PICKED and IN_TRANSIT.
* [OLMIS-3954](https://openlmis.atlassian.net/browse/OLMIS-3954): Updated order status to FULFILLING when creating shipment draft and ORDERED when deleting.
* [OLMIS-3952](https://openlmis.atlassian.net/browse/OLMIS-3952): Created order status is set to ORDERED based on supplying facility program fulfilling setting.
* [OLMIS-3826](https://openlmis.atlassian.net/browse/OLMIS-3826): Requesting facilities endpoint now accepts multiple supplyingFacilityId parameters.
* [OLMIS-4168](https://openlmis.atlassian.net/browse/OLMIS-4168): Made sure that PoD endpoints will use new rights
* [OLMIS-4240](https://openlmis.atlassian.net/browse/OLMIS-4240): Updated orders endpoint to respect user rights
* [OLMIS-4078](https://openlmis.atlassian.net/browse/OLMIS-4078): Removed line item validation from shipment draft to allow starting order fulfillment even if no matching stock cards.
* [OLMIS-4216](https://openlmis.atlassian.net/browse/OLMIS-4216): Fixed problem with saving an order if supplying facility does not support a program
* [OLMIS-4281](https://openlmis.atlassian.net/browse/OLMIS-4281): Updated Orderable service to use new reference data API
* [OLMIS-4500](https://openlmis.atlassian.net/browse/OLMIS-4500): Split huge requests to other services into smaller chunks
* [OLMIS-4452](https://openlmis.atlassian.net/browse/OLMIS-4452): Fixed filtering Orders by Processing Period start/end date:
  * Now if any day from period matches filter interval it will be returned.
  * Fixed issues with result pagination.

6.1.0 / 2017-11-09
==================

New functionality added in a backwards-compatible manner:
* [OLMIS-3221](https://openlmis.atlassian.net/browse/OLMIS-3221): Added period start and end dates parameters to the order search endpoint

Improvements added in a backwards-compatible manner:
* [OLMIS-3112](https://openlmis.atlassian.net/browse/OLMIS-3206): Added OrderNumberGenerator extension point. Changed the default implementation to provide 8 character, base36 order numbers.

6.0.0 / 2017-09-01
==================

Contract breaking changes:
* [OLMIS-2898](https://openlmis.atlassian.net/browse/OLMIS-2898): Changed POD receivedDate from ZonedDateTime to LocalDate.

New functionality added in a backwards-compatible manner:
* [OLMIS-2724](https://openlmis.atlassian.net/browse/OLMIS-2724): Added an endpoint for retrieving all the available, distinct requesting facilities.
* [OLMIS-2851](https://openlmis.atlassian.net/browse/OLMIS-2851): Let external applications (that run in a browser) access our APIs
  * Add CORS support.

Bug fixes and improvements (backwards-compatible):
* [OLMIS-2871](https://openlmis.atlassian.net/browse/OLMIS-2871): The service now uses an Authorization header instead of an access_token request parameter when communicating with other services.
* [OLMIS-3059](https://openlmis.atlassian.net/browse/OLMIS-3059): The search orders endpoint now sorts the orders by created date property (most recent first).
* [OLMIS-3045](https://openlmis.atlassian.net/browse/OLMIS-3045): Fix Single Order Report
  * Display the approvedQuantity rather than orderedQuantity. Note that OrderController loads this JRXML file directly from disk. It is therefore unnecessary to insert a compiled version of it into the database via a migration script.


5.0.0 / 2017-07-20
==================

Contract breaking changes:
* [OLMIS-2612](https://openlmis.atlassian.net/browse/OLMIS-2612): Configuration settings endpoints
(/api/configurationSettings) are no longer available. Use environment variables to configure the application.

Bug fixes and improvements (backwards-compatible):
* [OLMIS-2795](https://openlmis.atlassian.net/browse/OLMIS-2795): Added missing REJECTED status to the fulfillment domain.
* [OLMIS-2850](https://openlmis.atlassian.net/browse/OLMIS-2850): Fix to generateMigration being run too often.

4.0.0 / 2017-06-23
==================

New functionality added in a backwards-compatible manner:
* [OLMIS-2551](https://openlmis.atlassian.net/browse/OLMIS-2551): Added an endpoint for batch
order creation, which allows creating multiple orders in one transaction
* Added pricePerPack to ProgramOrderableDto
* [OLMIS-2611](https://openlmis.atlassian.net/browse/OLMIS-2611): Added using locale from env file

Performance improvements added in a backwards-compatible manner:
* Removed Products with Zero-Quantity Values from Order Export File
* Set *LAZY* flag for all collections inside Order and Proof of Delivery classes
* A single request with orderable IDs will be sent to reference-data service when Order Dto object
is created
* Improve performance of view right checking for multiple orders
* Search endpoint performance fixes:
  * Create basic order dto object only for orders that would be returned.
  * Moved sort command into database layer.

Contract breaking changes:
* Order endpoints (except for *POST /orders* and *GET /orders/{id}*) will now return new, smaller
Dto object, which only contains basic information about the order.

3.0.3 / 2017-05-26
==================

Bug fixes and improvements (backwards-compatible):

* [OLMIS-1696](https://openlmis.atlassian.net/browse/OLMIS-1696): removed redundant fields
from ProgramOrderableDto
  * Adjusted report to changes in referencedata database.
* [OLMIS-2484](https://openlmis.atlassian.net/browse/OLMIS-2484): added FTP properties demo-data
for Balaka District Warehouse

3.0.2 / 2017-05-08
==================
Dev and tooling updates made in a backwards-compatible manner:

* [OLMIS-1972](https://openlmis.atlassian.net/browse/OLMIS-1972): Update Postgres from 9.4 to 9.6
  * This upgrade will apply automatically and all data will migrate.
* Update [Docker Dev Image](https://github.com/OpenLMIS/docker-dev) for builds from v1 to v2
  * Moves the sync_transifex.sh script out of each service and into the Docker Dev Image.
* [OLMIS-2155](https://openlmis.atlassian.net/browse/OLMIS-2155): Use the date converter from
Hibernate for Java 8 in order to improve persistence performance

3.0.1 / 2017-03-29
==================

Bug fixes, security and performance improvements (backwards-compatible):

* [OLMIS-1395](https://openlmis.atlassian.net/browse/OLMIS-1395): Print Order PDF
  * Improve the formatting of the PDF when printing an order.
* [OLMIS-1428](https://openlmis.atlassian.net/browse/OLMIS-1428): Forbid creating new order file
template
  * Only one order file template can exist in the system. If an order file template already
  exists, update it.
* [OLMIS-1453](https://openlmis.atlassian.net/browse/OLMIS-1453): Improve validation for transfer
 properties
  * This includes ftp and local transfer properties.
* [OLMIS-2002](https://openlmis.atlassian.net/browse/OLMIS-2002): Improve error handling for
printing a POD
* [OLMIS-2003](https://openlmis.atlassian.net/browse/OLMIS-2003): Forbid updating a POD after it
is submitted (confirmed)
* [OLMIS-2044](https://openlmis.atlassian.net/browse/OLMIS-2044): Fix print POD
* [OLMIS-2117](https://openlmis.atlassian.net/browse/OLMIS-2117): Improve error message for POD
template for printing

3.0.0 / 2017-03-01
==================

* Released openlmis-fulfillment 3.0.0 as part of openlmis-ref-distro 3.0.0. See [3.0.0 Release
Notes](https://openlmis.atlassian.net/wiki/display/OP/3.0.0+Release+Notes).
  * This was the first stable release of openlmis-fulfillment. It builds on the code, patterns,
  and lessons learned from OpenLMIS 1 and 2.
