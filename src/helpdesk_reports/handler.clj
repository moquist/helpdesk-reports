(ns helpdesk-reports.handler
  (:use compojure.core)
  (:require [cheshire.core :as cheshire]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as response]
            [helpdesk-reports.db :as db]))
        
(defroutes app-routes
  (GET "/" [] (response/resource-response "charts.html" {:root "public"}))
  (GET "/tickets-grouped-by-creator.json" 
    {params :params} 
      (response/response 
        (cheshire/generate-string 
          { :params params 
            :data (db/tickets-grouped-by-creator params)})))
  (GET "/tickets-by-creator.json" 
    {params :params} 
      (response/response 
        (cheshire/generate-string 
          { :params params 
            :data (db/tickets-by-creator params) 
            :columns 
              ["id" "opened" "closed" "opendays" "creator" "requester" "resolver"]})))
  (GET "/tickets-grouped-by-resolver.json" 
    {params :params} 
      (response/response 
        (cheshire/generate-string 
          { :params params 
            :data  (db/tickets-grouped-by-resolver params)})))
  (GET "/tickets-by-resolver.json" 
    {params :params} 
     (response/response 
       (cheshire/generate-string 
         { :params params 
           :data (db/tickets-by-resolver params)
           :columns 
              ["id" "opened" "closed" "open_days" "creator" "requester" "resolver"]})))
  (GET "/open-tickets-grouped-by-owner.json" 
    {params :params} 
      (response/response 
        (cheshire/generate-string 
          { :params params 
            :data (db/open-tickets-grouped-by-owner params)})))
  (GET "/open-tickets-by-owner.json" 
    {params :params} 
      (response/response 
        (cheshire/generate-string 
          { :params params 
            :data (db/open-tickets-by-owner params)
            :columns 
               ["id" "opened" "open_days" "creator" "owner" "requester"]})))            
  (GET "/tickets-grouped-by-internal-user-wait-week.json"
    {params :params} 
      (response/response 
        (cheshire/generate-string 
          { :params params 
            :data (db/tickets-grouped-by-internal-user-wait-week params)})))  
  (GET "/tickets-by-internal-user-wait-week.json" 
    {params :params} 
      (response/response 
        (cheshire/generate-string 
          { :params params 
            :data (db/tickets-by-internal-user-wait-week params)
            :columns 
               ["id" "opened" "wait_days" "wait_over" "creator" "requester"]})))   
  (GET "/tickets-grouped-by-external-user-wait-week.json" 
        {params :params} 
        (response/response 
          (cheshire/generate-string 
            { :params params 
              :data (db/tickets-grouped-by-external-user-wait-week params)})))
  (GET "/tickets-by-external-user-wait-week.json" 
    {params :params} 
      (response/response 
        (cheshire/generate-string 
          { :params params 
            :data (db/tickets-by-external-user-wait-week params)
            :columns 
               ["id" "opened" "wait_days" "wait_over" "creator" "requester"]})))
  (GET "/tickets-grouped-by-month-created.json" 
        {params :params} 
        (response/response 
          (cheshire/generate-string 
            { :params params 
              :data (db/tickets-grouped-by-month-created params)})))
  (GET "/average-open-ticket-days-grouped-by-month-created.json" 
    {params :params} 
      (response/response 
        (cheshire/generate-string 
          { :params params 
            :data  { :overall (db/average-open-ticket-days-by-date-created params)
                     :months (db/average-open-ticket-days-grouped-by-month-created params)}})))
   (GET "/average-open-ticket-days-by-date-created.json" 
     {params :params} 
       (response/response 
         (cheshire/generate-string 
           { :params params 
             :data (db/average-open-ticket-days-by-date-created params)})))
  (GET "/average-first-response-wait-days-grouped-by-month-created.json" 
    {params :params} 
      (response/response 
        (cheshire/generate-string 
          { :params params 
            :data  { :overall (db/average-first-response-wait-days-by-date-created params)
                     :months (db/average-first-response-wait-days-grouped-by-month-created params)}})))
  (GET "/open-tickets-at-start-of-months.json" 
    {params :params} 
      (response/response 
        (cheshire/generate-string 
          { :params params 
            :data (db/open-tickets-at-start-of-months params)})))

  (route/resources "/") 
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      (handler/api)))
