UPDATE fulfillment.file_columns
SET
    position = 2
WHERE
    id = '33b2d2e9-3167-46b0-95d4-1295be9afc21';


UPDATE fulfillment.file_columns
SET
    position = 3
WHERE
    id = '752cda76-0db5-4b6e-bb79-0f531ab78e2e';


UPDATE fulfillment.file_columns
SET
    position = 4
WHERE
    id = '9e825396-269d-4873-baa4-89054e2722f5';


UPDATE fulfillment.file_columns
SET
    position = 6,
    datafieldlabel = 'fulfillment.header.approved.quantity'
WHERE
    id = 'cd57f329-f549-4717-882e-ecbf98122c39';

UPDATE fulfillment.file_columns
SET
    position = 7
WHERE
    id = 'd0e1aec7-1556-4dc1-8e21-d80a2d76b678';


UPDATE fulfillment.file_columns
SET
    position = 9
WHERE
    id = 'dab6eec0-4cb4-4d4c-94b7-820308da73ff';


UPDATE fulfillment.file_columns
SET
    position = 6,
    columnlabel='Customer ID',
    datafieldlabel = 'fulfillment.header.customer.id'
WHERE
    id = '6b8d331b-a0dd-4a1f-aafb-40e6a72ab9f6';