create table todo (
  id bigint primary key auto_increment,
  user_id bigint not null,
  title varchar(255) not null,
  done boolean not null default false,
  due_date date null,
  priority varchar(16) default 'NORMAL',
  sort_order int null,
  created_at timestamp default current_timestamp,
  updated_at timestamp default current_timestamp on update current_timestamp
);
create index idx_todo_user on todo(user_id, done, sort_order);
