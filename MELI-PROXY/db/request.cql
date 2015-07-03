CREATE TABLE meli.requests
(
   id varchar PRIMARY KEY,
   ip varchar,
   path varchar,
   http_code int,
   cache_hit int,
   response text,
   created_date timestamp
);

CREATE INDEX idx_request_ip ON meli.requests (ip);
CREATE INDEX idx_request_path ON meli.requests (path);
CREATE INDEX idx_request_http_code ON meli.requests (http_code);
CREATE INDEX idx_request_cache_hit ON meli.requests (cache_hit);
CREATE INDEX idx_request_date ON meli.requests (created_date);



