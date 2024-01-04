alter table hustoj.ranking_user
    add foreign key (contest_id) references hustoj.contest(id)
