MERGE INTO TB_USER_MSTR target
USING (
    SELECT
        'owner' || LPAD(SUBSTR(PARTNER_CODE, -2), 2, '0') || '@demo.routemate.kr' user_email,
        PARTNER_NAME || ' 대표' user_nicknm
    FROM TB_PARTNER_COMPANY
    WHERE PARTNER_CODE LIKE 'DEMO-%'
) source
ON (target.USER_EMAIL = source.user_email)
WHEN MATCHED THEN UPDATE SET
    target.USER_PWD = '$2a$10$82pOBoK4r8Uu0Amhql/B7uf3fO4MMqVzYX2264MadS.qMYNMmQrJy',
    target.USER_NICKNM = source.user_nicknm,
    target.USER_ROLE = 'PARTNER_OWNER',
    target.USER_STAT_CD = 'ACTIVE',
    target.DEL_YN = 'N'
WHEN NOT MATCHED THEN INSERT (
    USER_EMAIL, USER_PWD, USER_NICKNM, SNS_PROVIDER, USER_ROLE, USER_STAT_CD, DEL_YN
) VALUES (
    source.user_email, '$2a$10$82pOBoK4r8Uu0Amhql/B7uf3fO4MMqVzYX2264MadS.qMYNMmQrJy', source.user_nicknm,
    'LOCAL', 'PARTNER_OWNER', 'ACTIVE', 'N'
);

MERGE INTO TB_PARTNER_USER target
USING (
    SELECT partner.PARTNER_ID partner_id, user_mstr.USER_ID user_id
    FROM TB_PARTNER_COMPANY partner
    JOIN TB_USER_MSTR user_mstr
      ON user_mstr.USER_EMAIL = 'owner' || LPAD(SUBSTR(partner.PARTNER_CODE, -2), 2, '0') || '@demo.routemate.kr'
    WHERE partner.PARTNER_CODE LIKE 'DEMO-%'
) source
ON (target.USER_ID = source.user_id)
WHEN MATCHED THEN UPDATE SET
    target.PARTNER_ID = source.partner_id,
    target.PARTNER_ROLE = 'OWNER',
    target.USE_YN = 'Y',
    target.MDFY_DT = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (PARTNER_ID, USER_ID, PARTNER_ROLE, USE_YN)
VALUES (source.partner_id, source.user_id, 'OWNER', 'Y');
