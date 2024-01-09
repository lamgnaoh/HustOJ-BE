create table hustoj.token
(
    id          bigint auto_increment primary key,
    created_at  datetime(6) null,
    updated_at  datetime(6) null,
    token        varchar(500) ,
    expires_at datetime ,
    confirmed_at      datetime null,
    user_id   bigint not null,
    constraint tokenUserFK
        foreign key (user_id) references hustoj.user (id)
);