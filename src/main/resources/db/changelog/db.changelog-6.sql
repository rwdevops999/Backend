CREATE SEQUENCE IF NOT EXISTS public.bucket_seq
INCREMENT 1
START 1
MINVALUE 1
MAXVALUE 9223372036854775807
CACHE 1;

ALTER SEQUENCE public.bucket_seq OWNER TO postgres;