CREATE TABLE public.symbols_data(
                             id UUID PRIMARY KEY,
                             symbol VARCHAR(16) NOT NULL,
                             price NUMERIC(30, 15) NOT NULL,
                             fetched_at TIMESTAMP WITH TIME ZONE NOT NULL
)