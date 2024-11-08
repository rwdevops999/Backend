CREATE SEQUENCE IF NOT EXISTS public.file_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.file_seq
    OWNER TO postgres;