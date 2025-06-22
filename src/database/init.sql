CREATE TABLE IF NOT EXISTS appointment (
                                        id SERIAL PRIMARY KEY,                          -- Автоинкрементируемый первичный ключ
                                        telephone VARCHAR(255) NOT NULL,                -- Номер телефона клиента
                                        recommend TEXT NOT NULL,                        -- Рекомендация косметолога
                                        appointment_date TIMESTAMP NOT NULL,            -- Дата и время визита
                                        photo_base64 TEXT                              -- Новое поле для хранения изображения в виде BASE64
    );

CREATE TABLE appointment_photos (
                                    id SERIAL PRIMARY KEY,
                                    appointment_id BIGINT REFERENCES appointment(id),
                                    photo_base64 TEXT
);


CREATE OR REPLACE FUNCTION clean_old_record_if_needed()
    RETURNS TRIGGER AS $$
BEGIN
    -- Узнать текущее количество записей
    IF (SELECT COUNT(*) FROM appointment) >= 50 THEN
-- Удаляем самую старую запись
DELETE FROM appointment
WHERE id = (SELECT MIN(id) FROM appointment);
END IF;

    -- Возвращаем новую запись для вставки
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER before_insert_cleaner
    BEFORE INSERT ON appointment
    FOR EACH ROW
    EXECUTE FUNCTION clean_old_record_if_needed();