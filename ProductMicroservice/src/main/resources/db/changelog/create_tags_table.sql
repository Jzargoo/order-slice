CREATE TABLE product_tags(
    product_id BIGINT REFERENCES products,
    tag VARCHAR(255) NOT NULL,
    PRIMARY KEY (product_id,tag)
);
