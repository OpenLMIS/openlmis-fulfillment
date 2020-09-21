

INSERT INTO file_templates (id, fileprefix, headerinfile, templatetype) VALUES ('8280fc97-a44d-4630-9f42-fb343838eb39', 'B', true, 'BUDGET');


INSERT INTO file_columns (id, columnlabel, datafieldlabel, format, include, keypath, nested, openlmisfield, "position", related, relatedkeypath, filetemplateid) VALUES('868eed77-4ab5-4211-a8e6-c25d8a4f2816', 'Name', 'fulfillment.header.budget.name', NULL, true, 'name', 'name', true, 1, NULL, NULL, '8280fc97-a44d-4630-9f42-fb343838eb39');
INSERT INTO file_columns (id, columnlabel, datafieldlabel, format, include, keypath, nested, openlmisfield, "position", related, relatedkeypath, filetemplateid) VALUES ('39ee56fb-2aa2-472c-b3b5-abf9befa5c34', 'Zone', 'fulfillment.header.budget.zone', NULL, true, 'zone', 'zone', true, 2, 'zone', 'zone', '8280fc97-a44d-4630-9f42-fb343838eb39');
INSERT INTO file_columns (id, columnlabel, datafieldlabel, format, include, keypath, nested, openlmisfield, "position", related, relatedkeypath, filetemplateid) VALUES ('5940412a-0350-41ee-b4cb-eccaa2e6622c', 'Total Sales', 'fulfillment.header.budget.total.sales', NULL, true, 'totalSales', 'totalSales', true, 3, 'totalSales', 'totalSales', '8280fc97-a44d-4630-9f42-fb343838eb39');
INSERT INTO file_columns (id, columnlabel, datafieldlabel, format, include, keypath, nested, openlmisfield, "position", related, relatedkeypath, filetemplateid) VALUES ('63b23202-acff-451a-ac99-59b3b1d1d9f6', 'Total Deposits', 'fulfillment.header.budget.total.deposits', NULL, true, 'totalDeposits', 'totalDeposits', true, 4, 'totalDeposits', 'totalDeposits', '8280fc97-a44d-4630-9f42-fb343838eb39');
INSERT INTO file_columns (id, columnlabel, datafieldlabel, format, include, keypath, nested, openlmisfield, "position", related, relatedkeypath, filetemplateid) VALUES ('8b8e64e4-7e8e-4fed-bd41-d06892b5dde3', 'Opening Balance', 'fulfillment.header.budget.opening.balance', NULL, true, 'openingBalance', 'openingBalance', true, 5, NULL, NULL, '8280fc97-a44d-4630-9f42-fb343838eb39');
INSERT INTO file_columns (id, columnlabel, datafieldlabel, format, include, keypath, nested, openlmisfield, "position", related, relatedkeypath, filetemplateid) VALUES ('94dd7715-db20-4310-8f2a-b1477ee29468', 'Closing Balance', 'fulfillment.header.budget.closing.balance', NULL, true, 'closingBalance', 'closingBalance', true, 6, NULL, NULL, '8280fc97-a44d-4630-9f42-fb343838eb39');


