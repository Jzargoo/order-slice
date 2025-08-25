CREATE TABLE reservation(
    id BIGSERIAL PRIMARY KEY ,
    product_id bigint NOT NULL ,
    order_id bigint NOT NULL ,
    quantity int,
    status varchar(32),
    created_at timestamp DEFAULT current_timestamp,
    expiration_date timestamp
);