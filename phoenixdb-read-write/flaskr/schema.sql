-- Do things on only one line to make parsing easier
drop sequence if exists flaskr_user_seq;
create sequence flaskr_user_seq;
drop table if exists user;
create table user (id INTEGER not null primary key, username varchar, password varchar);
drop sequence if exists flaskr_post_seq;
create sequence flaskr_post_seq;
drop table if exists post;
create table post (id integer not null, author_id integer not null, created timestamp, title varchar, body varchar, CONSTRAINT pk PRIMARY KEY (id, author_id));
