CREATE TABLE IF NOT EXISTS user (
                                    _id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    name VARCHAR(40) NOT NULL UNIQUE,
                                    password VARCHAR(255) NOT NULL,
                                    photo LONGBLOB,
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS message (
                                       _id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       sender BIGINT NOT NULL,
                                       receiver BIGINT NOT NULL,
                                       message VARCHAR(200) NOT NULL,
                                       ddate DATE NOT NULL,
                                       `read` INT NOT NULL,
                                       reserved VARCHAR(200),
                                       FOREIGN KEY (sender) REFERENCES user(_id),
                                       FOREIGN KEY (receiver) REFERENCES user(_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;