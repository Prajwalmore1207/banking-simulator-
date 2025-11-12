-- Users table for authentication
CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(15),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    is_active BOOLEAN DEFAULT 1
);

-- Accounts table
CREATE TABLE IF NOT EXISTS accounts (
    account_number VARCHAR(20) PRIMARY KEY,
    user_id INTEGER NOT NULL,
    account_holder_name VARCHAR(100) NOT NULL,
    balance DECIMAL(15,2) DEFAULT 0.00,
    account_type VARCHAR(20) DEFAULT 'SAVINGS',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    min_balance DECIMAL(15,2) DEFAULT 100.00,
    is_active BOOLEAN DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- Transactions table
CREATE TABLE IF NOT EXISTS transactions (
    transaction_id VARCHAR(50) PRIMARY KEY,
    from_account VARCHAR(20),
    to_account VARCHAR(20),
    amount DECIMAL(15,2) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    description TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status BOOLEAN DEFAULT 1,
    FOREIGN KEY (from_account) REFERENCES accounts(account_number),
    FOREIGN KEY (to_account) REFERENCES accounts(account_number)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_accounts_user_id ON accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_from_account ON transactions(from_account);
CREATE INDEX IF NOT EXISTS idx_transactions_to_account ON transactions(to_account);
CREATE INDEX IF NOT EXISTS idx_transactions_timestamp ON transactions(timestamp);