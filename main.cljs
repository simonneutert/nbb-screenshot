(ns script
  (:require
   ["playwright$default" :as playwright :refer [firefox]]
   ["argparse" :as argparse :refer [ArgumentParser]]))

(defn- sleep [f ms]
  (js/setTimeout f ms))

(def parser
  (ArgumentParser. #js {:prog "main.cljs"
                        :description "This is a simple screenshot app!"}))

;; positional argument url
(.add_argument parser ""
               #js {:help "url of which to take a screenshot"})
;; optional arguments
;; timeout
(.add_argument parser "-t" "--timeout"
               #js {:help "timeout in milliseconds after page visit, before taking the screenshot"
                    :type "int"})
;; fullscreen
(.add_argument parser "-a" "--allscreen"
               #js {:help "take a fullscreen screenshot, default is viewport"
                    :action "store_true"})

(defn- get-url-command-line-arg
  "Parses the first string after calling the script

  `$ nbb main.cljs https://www.simon-neutert.de`"
  []
  (.- (.parse_args parser (clj->js (vec *command-line-args*)))))

(defn- get-timeout-command-line-arg
  "Parses the argument passed with `-t` or `--timeout` after calling the script
   defaults to 300ms

  `$ nbb main.cljs https://www.simon-neutert.de -t 3000`"
  []
  (or
   (.-t (.parse_args parser (clj->js (vec *command-line-args*))))
   (.-timeout (.parse_args parser (clj->js (vec *command-line-args*))))
   300))

(defn- get-fullscreen-command-line-arg
  "Parses the argument passed with `-a` or `--allscreen` after calling the script

  `$ nbb main.cljs https://www.simon-neutert.de -a`"
  []
  (or
   (.-a (.parse_args parser (clj->js (vec *command-line-args*))))
   (.-allscreen (.parse_args parser (clj->js (vec *command-line-args*))))
   false))

(defn sleep-promise+
  "wraps a Promise around a timeout returning the given object"
  [obj wait]
  (js/Promise.
   (fn [resolve _reject]
     (sleep #(resolve obj) wait))))

(defn url-without-protocol
  [url]
  (-> url
      (.replace "http://" "")
      (.replace "https://" "")))

(defn filename-screenshot
  [url]
  (prn url)
  (-> url
      (url-without-protocol)
      (str "_"  (str (js/Date.now)) ".png")))

(defn take-screenshot
  [page url]
  (if (get-fullscreen-command-line-arg)
    (-> (.screenshot page #js{:path (filename-screenshot url) :fullPage true}))
    (.screenshot page #js{:path (filename-screenshot url)})))

(defn screenshot
  []
  (let [url (get-url-command-line-arg)]
    (->
     (.launch firefox)
     (.then (fn [browser]
              (->
               (.newPage browser)
               (.then (fn [page]
                        (-> (.goto page url)
                            (.then #(sleep-promise+ browser (get-timeout-command-line-arg)))
                            (.then #(take-screenshot page url))
                            (.catch #(js/console.error %))
                            (.then #(.close browser)))))))))))

(screenshot)