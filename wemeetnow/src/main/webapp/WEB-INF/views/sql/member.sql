
create table member (
	email varchar2(50),
	pwd	varchar2(20) not null,
	nickname varchar2(50) not null,
	addr1 varchar2(100) not null,
	addr2 varchar2(100),
	addr3 varchar2(100),
	x_ varchar2(20) not null,
	y_ varchar2(20) not null,
	constraint member_pk primary key(email)
);

create sequence fc_seq increment by 1 start with 1 nocache;
insert into friend_confirm values(fc_seq.nextval(), '123@qq.qq','f1@qq.qq');

create table friend_confirm(
	num number(20),
	inviter varchar2(50) not null,
	invitee varchar2(50) not null,
	constraint friend_confirm_fk1 foreign key(inviter) references member(email),
	constraint friend_confirm_fk2 foreign key(invitee) references member(email),
	constraint friend_confirm_pk1 primary key(num)
);

create table friendship (
	fno number(20), 
	inviter varchar2(50),
	invitee varchar2(50),
	constraint friendship_fk1 foreign key(inviter) references member(email),
	constraint friendship_fk2 foreign key(invitee) references member(email),
	constraint friendship_pk primary key(fno)
);
insert into member values('123@qq.qq','123','123','123','123','123','123','123');
insert into member values('f1@qq.qq','123','123','123','123','123','123','123');
insert into member values('f2@qq.qq','123','123','123','123','123','123','123');
insert into member values('f3@qq.qq','123','123','123','123','123','123','123');
insert into member values('f4@qq.qq','123','123','123','123','123','123','123');
insert into member values('f5@qq.qq','123','123','123','123','123','123','123');
insert into member values('f6@qq.qq','123','123','123','123','123','123','123');
insert into member values('f7@qq.qq','123','123','123','123','123','123','123');


delete FRIENDSHIP where email2 ='2@1.qq';
delete FRIENDSHIP where email2 ='1@1.qq';
create sequence friend_seq increment by 1 start with 1 nocache;
create sequence route_slave_seq increment by 1 start with 1 nocache;

select * from member;
select * from friendship;
select * from FRIEND_CONFIRM
select * from tab;
SELECT * FROM ALL_TRIGGERS;

insert into friend_confirm values('jw@mail.com', 'js@mail.com', 0)