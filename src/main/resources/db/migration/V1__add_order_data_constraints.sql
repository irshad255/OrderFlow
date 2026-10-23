ALTER TABLE public.orders
    ADD CONSTRAINT chk_orders_quantity_range
        CHECK (quantity >= 1 AND quantity <= 100000);

ALTER TABLE public.orders
    ADD CONSTRAINT chk_orders_unit_price_positive
        CHECK (unit_price >= 0.01);