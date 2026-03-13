INSERT INTO users (user_id, username, email, password, user_role, active)
VALUES (
    gen_random_uuid(),
    'admin',
    '${admin_email}',
    '${admin_password}',
    'ROLE_ADMIN',
    true
);