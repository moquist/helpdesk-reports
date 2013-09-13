(ns helpdesk-reports.db
  (:use [korma.core]
        [environ.core :only [env]])
  (:require [clojure.string :as str]
            [korma.db :as kdb]))

(kdb/defdb db (kdb/postgres {:host (env :helpdesk-db-host)
                     :db (env :helpdesk-db-name)
                     :user (env :helpdesk-db-user)
                     :password (env :helpdesk-db-password)}))

(defn tickets-grouped-by-creator [params]
  (exec-raw ["
    select
       u.username,
       count(1)
    from 
      mdl_block_helpdesk_ticket t
      inner join mdl_block_helpdesk_hd_user hdu on (t.createdby = hdu.id)
      inner join mdl_user u on (u.id = hdu.userid)
    where 
          to_timestamp(t.timecreated) >= to_timestamp(?,'YYYY-MM-DD') 
      and to_timestamp(t.timecreated) <= to_timestamp(?,'YYYY-MM-DD') 
    group by t.createdby, u.username
    order by count desc
    limit ?" 
    [(if (str/blank? (:start params)) 
          "2000-01-01" 
          (:start params)) 
      (if (str/blank? (:end params)) 
          "3000-01-01" 
          (:end params)) 
      (Integer. (:top params 10))]] :results))

(defn tickets-by-creator [params]
  (exec-raw ["
    select
       t.id,
       to_char(to_timestamp(t.timecreated), 'YYYY-MM-DD') as opened,
       to_char(to_timestamp(tu.timecreated), 'YYYY-MM-DD') as closed,
       round(((coalesce(tu.timecreated,extract(epoch from now())) - t.timecreated) / 86400.00)::numeric, 1) as open_days,
       u_creator.username as creator,
       u_resolver.username as resolver,
       coalesce(hdu_requester.name, u_requester.username) as requester
    from 
      mdl_block_helpdesk_ticket t
      left outer join mdl_block_helpdesk_ticket_update tu 
        on (t.id = tu.ticketid and tu.newticketstatus in (3,4))
      inner join mdl_block_helpdesk_hd_user hdu_creator 
        on (t.createdby = hdu_creator.id)
      inner join mdl_user u_creator 
        on (u_creator.id = hdu_creator.userid)
      left outer join mdl_block_helpdesk_hd_user hdu_resolver
        on (hdu_resolver.id = tu.hd_userid)
      left outer join mdl_user u_resolver 
        on (u_resolver.id = hdu_resolver.userid)
      left outer join mdl_block_helpdesk_hd_user hdu_requester 
        on (t.hd_userid = hdu_requester.id)
      left outer join mdl_user u_requester 
        on (u_requester.id = hdu_requester.userid)
    where 
          to_timestamp(t.timecreated) >= to_timestamp(?,'YYYY-MM-DD') 
      and to_timestamp(t.timecreated) <= to_timestamp(?,'YYYY-MM-DD') 
      and u_creator.username = ?
    order by t.timecreated desc
    limit ?" 
    [(if (str/blank? (:start params)) 
          "2000-01-01" 
          (:start params)) 
      (if (str/blank? (:end params)) 
          "3000-01-01" 
          (:end params)) 
      (:name params) 
      (Integer. (:limit params 1000))]] :results))


(defn tickets-grouped-by-resolver [params]
  (exec-raw ["
    select
       u.username,
       count(1)
    from 
      mdl_block_helpdesk_ticket_update tu
      inner join mdl_block_helpdesk_hd_user hdu 
        on (tu.hd_userid = hdu.id)
      inner join mdl_user u 
        on (u.id = hdu.userid)
    where 
          to_timestamp(tu.timecreated) >= to_timestamp(?,'YYYY-MM-DD') 
      and to_timestamp(tu.timecreated) <= to_timestamp(?,'YYYY-MM-DD') 
      and tu.newticketstatus = 4
    group by tu.hd_userid, u.username
    order by count desc
    limit ?" 
    [(if (str/blank? (:start params)) 
          "2000-01-01" 
          (:start params)) 
      (if (str/blank? (:end params)) 
          "3000-01-01" 
          (:end params)) 
      (Integer. (:top params 10))]] :results))
      
(defn tickets-by-resolver [params]
  (exec-raw ["
    select 
      t.id,
      to_char(to_timestamp(t.timecreated), 'YYYY-MM-DD') as opened,
      to_char(to_timestamp(tu.timecreated), 'YYYY-MM-DD') as closed,
      round(((coalesce(tu.timecreated,extract(epoch from now())) - t.timecreated) / 86400.00)::numeric, 1) as open_days,
      u_creator.username as creator,
      u_resolver.username as resolver,
      coalesce(hdu_requester.name, u_requester.username) as requester
    from 
      mdl_block_helpdesk_ticket t
      inner join mdl_block_helpdesk_ticket_update tu
        on (t.id = tu.ticketid and tu.newticketstatus in (3,4))
      inner join mdl_block_helpdesk_hd_user hdu_creator 
        on (hdu_creator.id = t.createdby)
      inner join mdl_user u_creator 
        on (u_creator.id = hdu_creator.userid)
      inner join mdl_block_helpdesk_hd_user hdu_resolver
        on (hdu_resolver.id = tu.hd_userid)
      inner join mdl_user u_resolver 
        on (u_resolver.id = hdu_resolver.userid)
      inner join mdl_block_helpdesk_hd_user hdu_requester 
        on (t.hd_userid = hdu_requester.id)
      inner join mdl_user u_requester 
        on (u_requester.id = hdu_requester.userid)
        
    where 
          to_timestamp(tu.timecreated) >= to_timestamp(?,'YYYY-MM-DD') 
      and to_timestamp(tu.timecreated) <= to_timestamp(?,'YYYY-MM-DD') 
      and tu.hd_userid = (select id from mdl_block_helpdesk_hd_user where userid = (select id from mdl_user where mdl_user.username = ? limit 1))
    order by t.timecreated desc limit ?" 
    [(if (str/blank? (:start params)) 
          "2000-01-01" 
          (:start params)) 
      (if (str/blank? (:end params)) 
          "3000-01-01" 
          (:end params)) 
      (:name params) 
      (Integer. (:limit params 1000))]] :results))
      
(defn open-tickets-grouped-by-owner [params]
  (exec-raw ["
    select 
      count(1),
      coalesce(u.username, 'Unassigned') as username
    from 
      mdl_block_helpdesk_ticket t
      left outer join mdl_block_helpdesk_ticket_assign ta
        on (t.id = ta.ticketid)
      left outer join mdl_user u 
        on (u.id = ta.userid)
    where 
          to_timestamp(t.timecreated) >= to_timestamp(?,'YYYY-MM-DD') 
      and to_timestamp(t.timecreated) <= to_timestamp(?,'YYYY-MM-DD') 
      and t.status not in (3,4)
    group by u.username 
    order by count desc 
    limit ?" 
    [(if (str/blank? (:start params)) 
          "2000-01-01" 
          (:start params)) 
      (if (str/blank? (:end params)) 
          "3000-01-01" 
          (:end params)) 
      (Integer. (:top params 10))]] :results))

(defn open-tickets-by-owner [params]
  (if 
    (not= "Unassigned" (:name params))
    (exec-raw ["
      select
         t.id,
         to_char(to_timestamp(t.timecreated), 'YYYY-MM-DD') as opened,
         round(((extract(epoch from now()) - t.timecreated) / 86400.00)::numeric, 1) as open_days,
         u_creator.username as creator,
         u_owner.username as owner,
         coalesce(hdu_requester.name, u_requester.username) as requester
      from 
        mdl_block_helpdesk_ticket t
        left outer join mdl_block_helpdesk_ticket_assign ta
          on (t.id = ta.ticketid)
        left outer join mdl_user u_owner 
          on (u_owner.id = ta.userid)
        inner join mdl_block_helpdesk_hd_user hdu_creator 
          on (t.createdby = hdu_creator.id)
        inner join mdl_user u_creator 
          on (u_creator.id = hdu_creator.userid)
        inner join mdl_block_helpdesk_hd_user hdu_requester 
          on (t.hd_userid = hdu_requester.id)
        inner join mdl_user u_requester 
          on (u_requester.id = hdu_requester.userid)

      where 
            to_timestamp(t.timecreated) >= to_timestamp(?,'YYYY-MM-DD') 
        and to_timestamp(t.timecreated) <= to_timestamp(?,'YYYY-MM-DD') 
        and u_owner.username = ?
        and t.status not in (3,4)
      order by t.timecreated desc
      limit ?" 
      [(if (str/blank? (:start params)) 
            "2000-01-01" 
            (:start params)) 
        (if (str/blank? (:end params)) 
            "3000-01-01" 
            (:end params)) 
        (:name params) 
        (Integer. (:limit params 1000))]] :results)
    (exec-raw ["
      select
         t.id,
         to_char(to_timestamp(t.timecreated), 'YYYY-MM-DD') as opened,
         round(((extract(epoch from now()) - t.timecreated) / 86400.00)::numeric, 1) as open_days,
         u_creator.username as creator,
         u_owner.username as owner,
         coalesce(hdu_requester.name, u_requester.username) as requester
      from 
        mdl_block_helpdesk_ticket t
        left outer join mdl_block_helpdesk_ticket_assign ta
          on (t.id = ta.ticketid)
        left outer join mdl_user u_owner 
          on (u_owner.id = ta.userid)
        inner join mdl_block_helpdesk_hd_user hdu_creator 
          on (t.createdby = hdu_creator.id)
        inner join mdl_user u_creator 
          on (u_creator.id = hdu_creator.userid)
        inner join mdl_block_helpdesk_hd_user hdu_requester 
          on (t.hd_userid = hdu_requester.id)
        inner join mdl_user u_requester 
          on (u_requester.id = hdu_requester.userid)

      where 
            to_timestamp(t.timecreated) >= to_timestamp(?,'YYYY-MM-DD') 
        and to_timestamp(t.timecreated) <= to_timestamp(?,'YYYY-MM-DD') 
        and u_owner.username IS NULL
        and t.status not in (3,4)
      order by t.timecreated desc
      limit ?" 
      [(if (str/blank? (:start params)) 
            "2000-01-01" 
            (:start params)) 
        (if (str/blank? (:end params)) 
            "3000-01-01" 
            (:end params)) 
        (Integer. (:limit params 1000))]] :results)))

(defn tickets-grouped-by-internal-user-wait-week [params]
  (exec-raw ["
    with tickets as (
      select 
        t.id,
        t.timecreated,
        t.status
      from 
        mdl_block_helpdesk_ticket t
      where 
            to_timestamp(t.timecreated) >= to_timestamp(?,'YYYY-MM-DD')
        and to_timestamp(t.timecreated) < to_timestamp(?,'YYYY-MM-DD')
    ),
    target_ticket_updates as (
      select 
        tu.id,
        tu.ticketid,
        tu.timecreated
      from 
      ( 
        select 
          s_tu.ticketid,
          max(s_tu.id) as id
        from
          mdl_block_helpdesk_ticket_update s_tu
        where 
          s_tu.newticketstatus = 6
        group by 
          s_tu.ticketid
      ) s
    inner join 
      mdl_block_helpdesk_ticket_update tu 
        using (id)
    ), 
    ticket_waits as (
      select 
        t.id,
        ttu.id as uid,
        age(
          coalesce(
              to_timestamp(
                ( select 
                    min(timecreated)
                  from 
                    mdl_block_helpdesk_ticket_update stu
                  where 
                        stu.ticketid = ttu.ticketid
                    and newticketstatus <> 6
                    and stu.timecreated > ttu.timecreated
                )
              ),
              now()
          ), 
          to_timestamp(ttu.timecreated)
      ) as wait
      from
        tickets t 
        left outer join 
          target_ticket_updates ttu on (t.id = ttu.ticketid)
    ),
    waits_grouped_by_week as (
      select 
        s.week,
        count(s.week)
      from
        (
          select
            greatest(1,ceil(extract (day from tw.wait) / 7.00)) as week
          from
            ticket_waits tw
        ) s
      group by s.week
      order by s.week
    )
    select * from waits_grouped_by_week;"
    [(if (str/blank? (:start params)) 
            "2000-01-01" 
            (:start params)) 
        (if (str/blank? (:end params)) 
            "3000-01-01" 
            (:end params)) 
    ]] :results))

(defn tickets-by-internal-user-wait-week [params]
  (exec-raw ["
    with tickets as (
      select 
        t.id,
        t.timecreated,
        t.status
      from 
        mdl_block_helpdesk_ticket t
      where 
            to_timestamp(t.timecreated) >= to_timestamp(?,'YYYY-MM-DD')
        and to_timestamp(t.timecreated) < to_timestamp(?,'YYYY-MM-DD')
    ),
    target_ticket_updates as (
      select 
        tu.id,
        tu.ticketid,
        tu.timecreated
      from 
      ( 
        select
          s_tu.ticketid,
          max(s_tu.id) as id
        from
          mdl_block_helpdesk_ticket_update s_tu
        where 
          s_tu.newticketstatus = 6
        group by 
          s_tu.ticketid
      ) s
    inner join 
      mdl_block_helpdesk_ticket_update tu 
        using (id)
    ), 
    ticket_waits as (
      select 
        t.id,
        ttu.id as uid,
        age(
          coalesce(
              to_timestamp(
                ( select 
                    min(timecreated)
                  from 
                    mdl_block_helpdesk_ticket_update stu
                  where 
                        stu.ticketid = ttu.ticketid
                    and newticketstatus <> 6
                    and stu.timecreated > ttu.timecreated
                )
              ),
              now()
          ), 
          to_timestamp(ttu.timecreated)
      ) as wait
      from
        tickets t 
        left outer join 
          target_ticket_updates ttu on (t.id = ttu.ticketid)
    ),
    ticket_waits_by_week as (
      select 
        to_char(to_timestamp(t.timecreated), 'YYYY-MM-DD') as opened,
        extract (days from s.wait) as wait_days,
        u_creator.username as creator,
        coalesce(hdu_requester.name, u_requester.username) as requester,
        t.id,
        s.uid is not null as wait_over
      from
        (
          select
            tw.id,
            tw.uid,
            tw.wait
          from
            ticket_waits tw
          where 
            greatest(1,ceil(extract (day from tw.wait) / 7.00))::int = ? 
        ) s
      inner join mdl_block_helpdesk_ticket t 
        on (s.id = t.id)
      left outer join mdl_block_helpdesk_ticket_update tu
        on (s.uid = tu.id)
      left outer join mdl_block_helpdesk_hd_user hdu_creator 
        on (hdu_creator.id = t.createdby)
      left outer join mdl_user u_creator 
        on (u_creator.id = hdu_creator.userid)
      left outer join mdl_block_helpdesk_hd_user hdu_requester 
        on (t.hd_userid = hdu_requester.id)
      left outer join mdl_user u_requester 
        on (u_requester.id = hdu_requester.userid)
      order by opened
    )
    select * from ticket_waits_by_week" 
      [(if (str/blank? (:start params)) 
            "2000-01-01" 
            (:start params)) 
        (if (str/blank? (:end params)) 
            "3000-01-01" 
            (:end params)) 
        (Integer. (:week params))
        ]] :results))

(defn tickets-grouped-by-external-user-wait-week [params]
  (exec-raw ["
    with tickets as (
      select 
        t.id,
        t.timecreated,
        t.status
      from 
        mdl_block_helpdesk_ticket t
      where 
            to_timestamp(t.timecreated) >= to_timestamp(?,'YYYY-MM-DD')
        and to_timestamp(t.timecreated) < to_timestamp(?,'YYYY-MM-DD')

    ),
    target_ticket_updates as (
      select 
        tu.id,
        tu.ticketid,
        tu.timecreated
      from 
      ( 
        select 
          s_tu.ticketid,
          min(s_tu.id) as id
        from
          mdl_block_helpdesk_ticket_update s_tu
        where 
          s_tu.type = 'update_type_user'
        group by 
          s_tu.ticketid
      ) s
    inner join 
      mdl_block_helpdesk_ticket_update tu 
        using (id)
    ), 
    ticket_waits as (
      select 
        t.id,
        ttu.id as uid,
        age(coalesce(to_timestamp(ttu.timecreated), now()), to_timestamp(t.timecreated)) as wait
      from
        tickets t 
        left outer join 
          target_ticket_updates ttu on (t.id = ttu.ticketid)
    ),
    waits_grouped_by_week as (
      select 
        s.week,
        count(s.week)
      from
        (
          select
            greatest(1,ceil(extract (day from tw.wait) / 7.00)) as week
          from
            ticket_waits tw
        ) s
      group by s.week
      order by s.week
    )
    select * from waits_grouped_by_week;"
    [(if (str/blank? (:start params)) 
            "2000-01-01" 
            (:start params)) 
        (if (str/blank? (:end params)) 
            "3000-01-01" 
            (:end params)) 
    ]] :results))

(defn tickets-by-external-user-wait-week [params]
  (exec-raw ["
    with tickets as (
      select 
        t.id,
        t.timecreated,
        t.status
      from
        mdl_block_helpdesk_ticket t
      where 
            to_timestamp(t.timecreated) >= to_timestamp(?,'YYYY-MM-DD')
        and to_timestamp(t.timecreated) < to_timestamp(?,'YYYY-MM-DD')
    ),
    target_ticket_updates as (
      select 
        tu.id,
        tu.ticketid,
        tu.timecreated
      from 
      ( 
        select 
          s_tu.ticketid,
          min(s_tu.id) as id
        from
          mdl_block_helpdesk_ticket_update s_tu
        where 
          s_tu.type = 'update_type_user'
        group by 
          s_tu.ticketid
      ) s
    inner join 
      mdl_block_helpdesk_ticket_update tu 
        using (id)
    ), 
    ticket_waits as (
      select 
        t.id,
        ttu.id as uid,
        age(coalesce(to_timestamp(ttu.timecreated), now()), to_timestamp(t.timecreated)) as wait
      from
        tickets t 
        left outer join 
          target_ticket_updates ttu on (t.id = ttu.ticketid)
    ),
    ticket_waits_by_week as (
      select 
        to_char(to_timestamp(t.timecreated), 'YYYY-MM-DD') as opened,
        extract (days from s.wait) as wait_days,
        u_creator.username as creator,
        coalesce(hdu_requester.name, u_requester.username) as requester,
        t.id,
        s.uid is not null as wait_over
      from
        (
          select
            tw.id,
            tw.uid,
            tw.wait
          from
            ticket_waits tw
          where 
            greatest(1,ceil(extract (day from tw.wait) / 7.00))::int = ? 
        ) s
      inner join mdl_block_helpdesk_ticket t 
        on (s.id = t.id)
      left outer join mdl_block_helpdesk_ticket_update tu
        on (s.uid = tu.id)
      left outer join mdl_block_helpdesk_hd_user hdu_creator 
        on (hdu_creator.id = t.createdby)
      left outer join mdl_user u_creator 
        on (u_creator.id = hdu_creator.userid)
      left outer join mdl_block_helpdesk_hd_user hdu_requester 
        on (t.hd_userid = hdu_requester.id)
      left outer join mdl_user u_requester 
        on (u_requester.id = hdu_requester.userid)
      order by opened
    )
    select * from ticket_waits_by_week" 
      [(if (str/blank? (:start params)) 
            "2000-01-01" 
            (:start params)) 
        (if (str/blank? (:end params)) 
            "3000-01-01" 
            (:end params)) 
        (Integer. (:week params))
        ]] :results))

(defn tickets-by-external-user-wait-week [params]
  (exec-raw ["
    with tickets as (
      select 
        t.id,
        t.timecreated,
        t.status
      from
        mdl_block_helpdesk_ticket t
      where 
            to_timestamp(t.timecreated) >= to_timestamp(?,'YYYY-MM-DD')
        and to_timestamp(t.timecreated) < to_timestamp(?,'YYYY-MM-DD')
    ),
    target_ticket_updates as (
      select 
        tu.id,
        tu.ticketid,
        tu.timecreated
      from 
      ( 
        select 
          s_tu.ticketid,
          min(s_tu.id) as id
        from
          mdl_block_helpdesk_ticket_update s_tu
        where 
          s_tu.type = 'update_type_user'
        group by 
          s_tu.ticketid
      ) s
    inner join 
      mdl_block_helpdesk_ticket_update tu 
        using (id)
    ), 
    ticket_waits as (
      select 
        t.id,
        ttu.id as uid,
        age(coalesce(to_timestamp(ttu.timecreated), now()), to_timestamp(t.timecreated)) as wait
      from
        tickets t 
        left outer join 
          target_ticket_updates ttu on (t.id = ttu.ticketid)
    ),
    ticket_waits_by_week as (
      select 
        to_char(to_timestamp(t.timecreated), 'YYYY-MM-DD') as opened,
        extract (days from s.wait) as wait_days,
        u_creator.username as creator,
        coalesce(hdu_requester.name, u_requester.username) as requester,
        t.id,
        s.uid is not null as wait_over
      from
        (
          select
            tw.id,
            tw.uid,
            tw.wait
          from
            ticket_waits tw
          where 
            greatest(1,ceil(extract (day from tw.wait) / 7.00))::int = ? 
        ) s
      inner join mdl_block_helpdesk_ticket t 
        on (s.id = t.id)
      left outer join mdl_block_helpdesk_ticket_update tu
        on (s.uid = tu.id)
      left outer join mdl_block_helpdesk_hd_user hdu_creator 
        on (hdu_creator.id = t.createdby)
      left outer join mdl_user u_creator 
        on (u_creator.id = hdu_creator.userid)
      left outer join mdl_block_helpdesk_hd_user hdu_requester 
        on (t.hd_userid = hdu_requester.id)
      left outer join mdl_user u_requester 
        on (u_requester.id = hdu_requester.userid)
      order by opened
    )
    select * from ticket_waits_by_week" 
      [(if (str/blank? (:start params)) 
            "2000-01-01" 
            (:start params)) 
        (if (str/blank? (:end params)) 
            "3000-01-01" 
            (:end params)) 
        (Integer. (:week params))
        ]] :results))

(defn tickets-grouped-by-month-created [params]
  (exec-raw ["
    select 
      extract (month from to_timestamp(t.timecreated)) as month,
      extract (year from to_timestamp(t.timecreated)) as year,
      count(1)
    from 
      mdl_block_helpdesk_ticket t
    where 
          to_timestamp(t.timecreated) >= to_timestamp(?,'YYYY-MM-DD') 
      and to_timestamp(t.timecreated) <= to_timestamp(?,'YYYY-MM-DD') 
    group by month, year 
    order by year asc, month asc" 
    [(if (str/blank? (:start params)) 
          "2000-01-01" 
          (:start params)) 
      (if (str/blank? (:end params)) 
          "3000-01-01"
           (:end params))]] :results))

(defn average-open-ticket-days-grouped-by-month-created [params]
  (exec-raw ["
    select 
      extract (month from to_timestamp(t.timecreated)) as month,
      extract (year from to_timestamp(t.timecreated)) as year,
      round(avg((coalesce(tu.timecreated,extract(epoch from now())) - t.timecreated) / 86400)) as avgDays,
      count(1) as tickets
    from 
      mdl_block_helpdesk_ticket t
      left outer join mdl_block_helpdesk_ticket_update tu
        on (t.id = tu.ticketid and tu.newticketstatus in (3,4))
    where 
          to_timestamp(t.timecreated) >= to_timestamp(?,'YYYY-MM-DD') 
      and to_timestamp(t.timecreated) <= to_timestamp(?,'YYYY-MM-DD') 
    group by month, year 
    order by year asc, month asc" 
    [(if (str/blank? (:start params)) 
          "2000-01-01" 
          (:start params)) 
      (if (str/blank? (:end params)) 
          "3000-01-01"
           (:end params))]] :results))

(defn average-first-response-wait-days-grouped-by-month-created [params]
 (exec-raw ["
   select 
     extract (month from to_timestamp(t.timecreated)) as month,
     extract (year from to_timestamp(t.timecreated)) as year,
     round(avg((coalesce(ttu.timecreated,extract(epoch from now())) - t.timecreated) / 86400)) as avgDays,
     count(1) as tickets
   from 
     mdl_block_helpdesk_ticket t
     left outer join 
       (
           select 
             tu.ticketid,
             min(tu.timecreated) as timecreated
           from mdl_block_helpdesk_ticket_update tu
           where
              tu.type = 'update_type_user'
           group by tu.ticketid
        )  ttu
       on (t.id = ttu.ticketid)
    where 
         to_timestamp(t.timecreated) >= to_timestamp(?,'YYYY-MM-DD') 
     and to_timestamp(t.timecreated) <= to_timestamp(?,'YYYY-MM-DD') 
   group by month, year 
   order by year asc, month asc" 
   [(if (str/blank? (:start params)) 
         "2000-01-01" 
         (:start params)) 
     (if (str/blank? (:end params)) 
         "3000-01-01"
          (:end params))]] :results))

(defn open-tickets-at-start-of-months [params]
 (exec-raw ["
   select 
     extract (month from to_timestamp(t.timecreated)) as month,
     extract (year from to_timestamp(t.timecreated)) as year,
     count(1) as tickets
   from 
     mdl_block_helpdesk_ticket t
    where 
         to_timestamp(t.timecreated) >= to_timestamp(?,'YYYY-MM-DD') 
     and to_timestamp(t.timecreated) <= to_timestamp(?,'YYYY-MM-DD') 
     and t.status not in (3,4)
   group by month, year 
   order by year asc, month asc" 
   [(if (str/blank? (:start params)) 
         "2000-01-01" 
         (:start params)) 
     (if (str/blank? (:end params)) 
         "3000-01-01"
          (:end params))]] :results))

(defn average-open-ticket-days-by-date-created[params]
 (exec-raw ["
   select 
     round(avg((coalesce(tu.timecreated,extract(epoch from now()))) - t.timecreated) / 86400) as avgdays,
     count(1) as tickets
   from 
     mdl_block_helpdesk_ticket t
     left outer join mdl_block_helpdesk_ticket_update tu
       on (t.id = tu.ticketid and tu.newticketstatus in (3,4))
   where 
         to_timestamp(t.timecreated) >= to_timestamp(?,'YYYY-MM-DD') 
     and to_timestamp(t.timecreated) <= to_timestamp(?,'YYYY-MM-DD')" 
   [(if (str/blank? (:start params)) 
         "2000-01-01" 
         (:start params)) 
     (if (str/blank? (:end params)) 
         "3000-01-01"
          (:end params))]] :results))
          
(defn average-first-response-wait-days-by-date-created[params]
 (exec-raw ["
   select 
     round(avg((coalesce(ttu.timecreated,extract(epoch from now()))) - t.timecreated) / 86400) as avgdays,
     count(1) as tickets
   from 
     mdl_block_helpdesk_ticket t
     left outer join 
       (
           select 
             tu.ticketid,
             min(tu.timecreated) as timecreated
           from mdl_block_helpdesk_ticket_update tu
           where
              tu.type = 'update_type_user'
           group by tu.ticketid
        )  ttu
       on (t.id = ttu.ticketid)
    where 

         to_timestamp(t.timecreated) >= to_timestamp(?,'YYYY-MM-DD') 
     and to_timestamp(t.timecreated) <= to_timestamp(?,'YYYY-MM-DD')" 
   [(if (str/blank? (:start params)) 
         "2000-01-01" 
         (:start params)) 
     (if (str/blank? (:end params)) 
         "3000-01-01"
          (:end params))]] :results))


      

