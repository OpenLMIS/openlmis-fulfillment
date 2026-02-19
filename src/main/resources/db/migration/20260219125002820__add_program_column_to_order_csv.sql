INSERT INTO fulfillment.file_columns (
    id,
    columnlabel,
    datafieldlabel,
    format,
    include,
    keypath,
    nested,
    openlmisfield,
    "position",
    related,
    relatedkeypath,
    fileTemplateId
)
VALUES (
           'c795ea63-bbf4-4cfe-bac3-7fc1e8a4a29a',
           'Program code',
           'fulfillment.header.program.code',
           null,
           true,
           'programId',
           'order',
           true,
           8,
           'Program',
           'code',
           '457ed5b0-80d7-4cb6-af54-e3f6138c8128'
       )
    ON CONFLICT DO NOTHING;
