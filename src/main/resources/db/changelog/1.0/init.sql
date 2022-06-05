CREATE TABLE scripts (
    id    uuid default gen_random_uuid() primary key ,
    title varchar(100) not null,
    value text not null
);

INSERT INTO scripts(title, value) VALUES
                                      ('example script',
                                       '(function(record) {console.log(''input:'' + record.getId());})')