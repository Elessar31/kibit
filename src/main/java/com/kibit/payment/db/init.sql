CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE accounts (
                          id SERIAL PRIMARY KEY,
                          user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                          balance DECIMAL(15, 2) NOT NULL CHECK (balance >= 0),
                          currency VARCHAR(3) NOT NULL DEFAULT 'HUF',
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE transactions (
                              id SERIAL PRIMARY KEY,
                              sender_account_id INT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
                              receiver_account_id INT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
                              amount DECIMAL(15, 2) NOT NULL CHECK (amount > 0),
                              status VARCHAR(20) CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')) NOT NULL DEFAULT 'PENDING',
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE transaction_notifications (
                                           id SERIAL PRIMARY KEY,
                                           transaction_id INT NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
                                           recipient_email VARCHAR(100) NOT NULL,
                                           message TEXT NOT NULL,
                                           sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO public.users (id, name, email, created_at) VALUES (DEFAULT, 'Test', 'test@test.com', DEFAULT);
INSERT INTO public.accounts (id, user_id, balance, currency, created_at) VALUES (DEFAULT, 1, 1000.00, 'EUR', DEFAULT);
INSERT INTO public.accounts (id, user_id, balance, currency, created_at) VALUES (DEFAULT, 1, 300.00, 'USD', DEFAULT);