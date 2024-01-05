create table hustoj.issue
(
    id          bigint auto_increment primary key,
    created_at  datetime(6) null,
    updated_at  datetime(6) null,
    name        varchar(500) null,
    description text null,
    status      varchar(50) null,
    author_id   bigint not null,
    problem_id  bigint not null,
    constraint issueAuthorFK
        foreign key (author_id) references hustoj.user (id),
    constraint issueProblemFK
        foreign key (problem_id) references hustoj.problem (id)
);