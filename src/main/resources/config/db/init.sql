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
    id                       bigint  not null
        primary key,
    created_at               datetime(6)  null,
    updated_at               datetime(6)  null,
    ac_count                 bigint       null,
    ac_rate                  double       null,
    email                    varchar(50)  not null,
    enabled                  bit          not null,
    firstname                varchar(50)  null,
    lastname                 varchar(50)  null,
    name                     varchar(50)  not null,
    password                 varchar(100) not null,
    submit_count             bigint       null,
    username                 varchar(50)  null,
    last_password_reset_date datetime(6)  not null,
    constraint UK_ob8kqyqqgmefl0aco34akdtpe
        unique (email),
    constraint UK_sb8bbouer5wak8vyiiy4pf2bx
        unique (username)
);
INSERT INTO user (id,created_at, updated_at, ac_count, ac_rate, email, enabled, firstname, lastname, name, password, submit_count, username, last_password_reset_date)
VALUES ( 1,'2023-11-10 15:00:33.926486', '2023-11-10 15:00:33.926486', 0, 0, 'luonglamhoang2000@gmail.com', true, 'Hoang', 'Lam', 'Hoang Lam', '$2a$10$tPW8MzxVeso4K3ppE9546.NS7LTAW1VtEKS4T9Q3Qk1eHp6Qs6g4u', 0, 'root', '2023-11-13 00:00:00.000000');
INSERT INTO user (id,created_at, updated_at, ac_count, ac_rate, email, enabled, firstname, lastname, name, password, submit_count, username, last_password_reset_date)
VALUES ( 2,'2023-11-13 16:05:43.930083', '2023-11-13 16:05:43.930083', 0, 0, '821zziah@gmail.com', true, 'Walter', 'White', 'Walter White', '$2a$10$XOKPP3sVE/VmUmjM7SjKp.u7MG5ork1ff9YevXZX8CmNtsKqBCRee', 0, 'lamgnoah', '2023-11-13 16:05:43.817000');
INSERT INTO user (id,created_at, updated_at, ac_count, ac_rate, email, enabled, firstname, lastname, name, password, submit_count, username, last_password_reset_date)
VALUES (3,'2023-11-13 16:13:02.689830', '2023-11-13 16:13:02.689830', 0, 0, 'user1.oj@gmail.com', true, 'Nguyen Van', 'A', 'Nguyen Van A', '$2a$10$zqhuZ5WpyEGarJVyzwQVju.VSHH8.pEB6Gk6mzh/tHPnVlGLJkWSW', 0, 'user1', '2023-11-13 16:13:02.600000');

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


