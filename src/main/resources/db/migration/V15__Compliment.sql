CREATE TABLE compliment (
    compliment_id BIGINT NOT NULL AUTO_INCREMENT, 
    user_id BIGINT NOT NULL,                       
    password CHAR(4) NOT NULL,                    
    compliment_count INT DEFAULT 0,                
    compliment_title VARCHAR(255) NOT NULL,       
    compliment_detail TEXT,                        
    registration_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fix_time TIMESTAMP DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP, 
    PRIMARY KEY (compliment_id),
    FOREIGN KEY (user_id) REFERENCES users(id)    
);