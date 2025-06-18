CREATE TABLE IF NOT EXISTS user (
                                    _id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    name VARCHAR(40) NOT NULL UNIQUE,
                                    password VARCHAR(255) NOT NULL,
                                    photo LONGBLOB,
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE IF NOT EXISTS message (
                                       _id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       sender_id BIGINT NOT NULL,
                                       receiver_id BIGINT NOT NULL,
                                       content TEXT NOT NULL,
                                       sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       is_read BOOLEAN DEFAULT FALSE,
                                       is_delivered BOOLEAN DEFAULT FALSE,

                                       FOREIGN KEY (sender_id) REFERENCES user(_id),
                                       FOREIGN KEY (receiver_id) REFERENCES user(_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE INDEX idx_message_receiver ON message(receiver_id, is_delivered);
CREATE INDEX idx_message_sender ON message(sender_id);
