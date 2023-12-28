create database hustoj;
use hustoj;

create table if not exists authority
(
    id   bigint      not null
    primary key,
    name varchar(50) not null
    );
INSERT INTO authority (id, name) VALUES (1, 'ROLE_USER');
INSERT INTO authority (id, name) VALUES (2, 'ROLE_ADMIN');
INSERT INTO authority (id, name) VALUES (3, 'ROLE_SUPER_ADMIN');

create table user
(
    id                       bigint auto_increment
        primary key,
    created_at               datetime(6)                 null,
    updated_at               datetime(6)                 null,
    ac_count                 bigint                      null,
    ac_rate                  double                      null,
    email                    varchar(50)                 not null,
    enabled                  bit                         not null,
    firstname                varchar(50)                 null,
    last_password_reset_date datetime(6)                 not null,
    lastname                 varchar(50)                 null,
    name                     varchar(50)                 not null,
    password                 varchar(100)                not null,
    submit_count             bigint                      null,
    username                 varchar(50)                 null,
    problem_permission       enum ('ALL', 'NONE', 'OWN') null,
    total_score              int                         null,
    acm_problems_status      json default (_utf8mb4'{}') null,
    oi_problems_status       json default (_utf8mb4'{}') null,
    constraint UK_ob8kqyqqgmefl0aco34akdtpe
        unique (email),
    constraint UK_sb8bbouer5wak8vyiiy4pf2bx
        unique (username)
);

INSERT INTO hustoj.user (id, created_at, updated_at, ac_count, ac_rate, email, enabled, firstname, last_password_reset_date, lastname, name, password, submit_count, username, problem_permission, total_score, acm_problems_status, oi_problems_status) VALUES (1, '2023-11-10 15:00:33.926486', '2023-11-10 15:00:33.926486', 0, 0, 'luonglamhoang2000@gmail.com', true, 'Hoang', '2023-11-13 00:00:00.000000', 'Lam', 'Hoang Lam', '$2a$10$tPW8MzxVeso4K3ppE9546.NS7LTAW1VtEKS4T9Q3Qk1eHp6Qs6g4u', 0, 'root', 'ALL', 0, '{}', '{}');
INSERT INTO hustoj.user (id, created_at, updated_at, ac_count, ac_rate, email, enabled, firstname, last_password_reset_date, lastname, name, password, submit_count, username, problem_permission, total_score, acm_problems_status, oi_problems_status) VALUES (2, '2023-11-13 16:05:43.930083', '2023-12-20 15:25:40.887489', 0, 0, '821zziah@gmail.com', true, 'Walter', '2023-11-13 16:05:43.817000', 'White', 'Walter White', '$2a$10$XOKPP3sVE/VmUmjM7SjKp.u7MG5ork1ff9YevXZX8CmNtsKqBCRee', 0, 'lamgnoah', 'OWN', 0, '{}', '{}');
INSERT INTO hustoj.user (id, created_at, updated_at, ac_count, ac_rate, email, enabled, firstname, last_password_reset_date, lastname, name, password, submit_count, username, problem_permission, total_score, acm_problems_status, oi_problems_status) VALUES (3, '2023-11-13 16:13:02.689830', '2023-12-27 22:55:00.176914', 0, 0, 'user1.oj@gmail.com', true, 'Nguyen Van', '2023-11-13 16:13:02.600000', 'A', 'Nguyen Van A', '$2a$10$zqhuZ5WpyEGarJVyzwQVju.VSHH8.pEB6Gk6mzh/tHPnVlGLJkWSW', 0, 'user1', 'NONE', 0, '{}', '{}');


create table user_authority
(
    user_id      bigint not null,
    authority_id bigint not null,
    constraint FKgvxjs381k6f48d5d2yi11uh89
        foreign key (authority_id) references authority (id),
    constraint FKpqlsjpkybgos9w2svcri7j8xy
        foreign key (user_id) references user (id)
);

INSERT INTO hustoj.user_authority (user_id, authority_id) VALUES (1, 3);
INSERT INTO hustoj.user_authority (user_id, authority_id) VALUES (2, 2);
INSERT INTO hustoj.user_authority (user_id, authority_id) VALUES (3, 1);

create table problem
(
    id                 bigint auto_increment
        primary key,
    created_at         datetime(6)                    null,
    updated_at         datetime(6)                    null,
    accept_count       int                            null,
    accept_rate        double                         null,
    description        varchar(255)                   null,
    difficulty         enum ('HIGH', 'LOW', 'MEDIUM') null,
    hint               varchar(255)                   null,
    input_description  varchar(255)                   null,
    output_description varchar(255)                   null,
    problem_code       varchar(255)                   null,
    ram_limit          int                            null,
    sampleio           varchar(255)                   null,
    special_judged     bit                            null,
    submit_count       int                            null,
    time_limit         int                            null,
    title              varchar(255)                   null,
    visible            bit                            null,
    author_id          bigint                         null,
    create_in_contest  bit                            null,
    test_case_score    varchar(4000)                  null,
    total_score        int                            null,
    test_case_id       varchar(255)                   null,
    rule_type          enum ('ACM', 'OI')             null,
    constraint FK8d8a6n8oi9qjsb6m930msmo8v
        foreign key (author_id) references user (id)
);

create index UK_kpj6s1hrhhdneorsopcb9mgwr
    on problem (problem_code);

INSERT INTO hustoj.problem (id, created_at, updated_at, accept_count, accept_rate, description, difficulty, hint, input_description, output_description, problem_code, ram_limit, sampleio, special_judged, submit_count, time_limit, title, visible, author_id, create_in_contest, test_case_score, total_score, test_case_id, rule_type)
VALUES (1, '2023-12-01 18:12:34.986828', '2023-12-27 21:39:53.621239', 0, 0, '<p>Hãy tính tổng của hai số nguyên và in kết quả. Hãy cẩn thận để không có output không cần thiết, chẳng hạn như "Vui lòng nhập giá trị của a và b: ",<br></p>', 'LOW', '', '<p><span style="color: rgb(200, 195, 188);">Hai số nguyên cách nhau bởi dấu cách</span><br></p>', '<p>Tổng của 2 số</p>', 'HUST01', 256, '[{"input":"1 1","output":"2"},{"input":"2 3 ","output":"5"}]', false, 13, 1000, 'Simple A+B Problem', true, 1, false, '[{"input_name":"1.in","input_size":3,"output_name":"1.out","output_size":1,"score":"33","stripped_output_md5":"eccbc87e4b5ce2fe28308fd9f2a7baf3"},{"input_name":"2.in","input_size":3,"output_name":"2.out","output_size":1,"score":"33","stripped_output_md5":"e4da3b7fbbce2345d7772b0674a318d5"},{"input_name":"3.in","input_size":3,"output_name":"3.out","output_size":1,"score":"33","stripped_output_md5":"8f14e45fceea167a5a36dedd4bea2543"}]', 0, '98091d45-dda1-4409-a684-5bdef4bcdc79', 'ACM');


