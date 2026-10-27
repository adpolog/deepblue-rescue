
INSERT INTO rescue_centers (id, code, name, city) VALUES
(1, 'RC-SMR-01', 'Centro de Rescate Marino Santa Marta', 'Santa Marta');


INSERT INTO rescue_cases (id, case_code, rescue_date, rescue_location, status, rescue_center_id) VALUES
(1, 'CASE-2026-001', '2026-10-25', 'Playa Blanca', 'ADMITTED', 1);


INSERT INTO animals (id, animal_code, common_name, scientific_name, sex, rescue_case_id) VALUES
(1, 'ANI-001', 'Tortuga Carey', 'Eretmochelys imbricata', 'FEMALE', 1);


INSERT INTO medical_records (id, animal_id, initial_weight, initial_condition, injuries, observations) VALUES
(1, 1, 45.50, 'Deshidratación leve', 'Caparazón raspado', 'Responde bien a fluidos');


INSERT INTO specialists (id, professional_code, first_name, last_name, email, active) VALUES
(1, 'VET-001', 'Laura', 'Gómez', 'laura.gomez@deepblue.com', true);


INSERT INTO specialist_expertise (specialist_id, expertise_id) VALUES
(1, 1),
(1, 2);


INSERT INTO treatments (id, animal_id, specialist_id, performed_at, type, description) VALUES
(1, 1, 1, '2026-10-26 10:00:00', 'MEDICATION', 'Administración de suero intravenoso');

SELECT setval(pg_get_serial_sequence('rescue_centers', 'id'), (SELECT MAX(id) FROM rescue_centers));
SELECT setval(pg_get_serial_sequence('rescue_cases',   'id'), (SELECT MAX(id) FROM rescue_cases));
SELECT setval(pg_get_serial_sequence('animals',        'id'), (SELECT MAX(id) FROM animals));
SELECT setval(pg_get_serial_sequence('medical_records','id'), (SELECT MAX(id) FROM medical_records));
SELECT setval(pg_get_serial_sequence('specialists',    'id'), (SELECT MAX(id) FROM specialists));
SELECT setval(pg_get_serial_sequence('treatments',     'id'), (SELECT MAX(id) FROM treatments));