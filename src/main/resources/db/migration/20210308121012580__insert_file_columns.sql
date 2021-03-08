INSERT INTO fulfillment.file_columns (id, columnlabel, datafieldlabel, format, include, keypath, nested, openlmisfield, "position", related, relatedkeypath, filetemplateid)
VALUES ('ad545ee6-a221-498f-b4e7-1c4e053f9363', 'Plant', 'fulfillment.header.facility.zone', NULL, true, 'facilityZone', 'extraData', true, 1, NULL, NULL, '457ed5b0-80d7-4cb6-af54-e3f6138c8128');

INSERT INTO fulfillment.file_columns (id, columnlabel, datafieldlabel, format, include, keypath, nested, openlmisfield, "position", related, relatedkeypath, filetemplateid)
VALUES ('d6967c67-7ca3-4958-97f7-b1b1eccfa27d', 'Line Number', 'fulfillment.header.line.number', NULL, true, 'counter', 'line_no', true, 8, NULL, NULL, '457ed5b0-80d7-4cb6-af54-e3f6138c8128');

INSERT INTO fulfillment.file_columns (id, columnlabel, datafieldlabel, format, include, keypath, nested, openlmisfield, "position", related, relatedkeypath, filetemplateid)
VALUES ('351640d0-60bc-4b2d-a523-4c23cd4ef178', 'Price', 'fulfillment.header.price', NULL, true, 'price', 'lineItem', true, 10, NULL, NULL, '457ed5b0-80d7-4cb6-af54-e3f6138c8128');

