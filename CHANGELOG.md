6.0.0 / WIP
==================

Contract breaking changes:
* [OLMIS-2898](https://openlmis.atlassian.net/browse/OLMIS-2898): Changed POD receivedDate from ZonedDateTime to LocalDate.

New functionality added in a backwards-compatible manner:
* [OLMIS-2724](https://openlmis.atlassian.net/browse/OLMIS-2724): Added an endpoint for retrieving all the available, distinct requesting facilities.

Bug fixes and improvements (backwards-compatible):
* [OLMIS-2871](https://openlmis.atlassian.net/browse/OLMIS-2871): The service now uses an Authorization header instead of an access_token request parameter when communicating with other services.
* [OLMIS-3059](https://openlmis.atlassian.net/browse/OLMIS-3059): The search orders endpoint now sorts the orders by created date property (most recent first).

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
