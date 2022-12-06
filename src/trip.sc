theme: /Trip
    
    state: Begin
    #если заявку начинали оформлять то идем туда где остановились, иначе идем проверять/спрашивать имя
        if: $session.tripStep
            script: $reactions.transition ( {value:$session.tripStep, deferred: false} );
        else:
            random:
                a: Для оформления заявки я задам вам несколько вопросов. Обязательными будут только Ваше имя и телефон
                a: Чтобы оформить заявку, мне понадобятся ваши имя и номер телефона
            if: $client.name
                go!: /Trip/CheckName
            else:
                go!: /Trip/AskName
    
    #если имя уже есть - проверяем актуально ли оно
    state: CheckName
        random:
            a: Ваше имя {{$client.name}}, верно?
            a: Вас зовут {{$client.name}}, правильно?
        buttons:
            "Да"
            "Нет"
    #если да - идем на Шаг2 заявки   
        state: Yes
            q: $yes
            a: Внесла в заявку
            go!: /Trip/Step2
    #если нет - идем спрашивать имя
        state: No
            q: $no
            script: delete $client.name
            go!: /Trip/AskName
    #если отказ знакомиться - извиняемся и идем на выход
        state: Deny
            q: $refuse
            q: ( * (не надо)/(не хочу)/(не буду) * )
            a: К сожалению, без имени я не могу принять заявку на тур
            go!: /Exit
    #если нет внятного ответа - возвращаемся к вопросу
        state: CatchAll || noContext = true 
            event: noMatch
            a: Пожалуйста, ответьте да или нет:
            go!: /Trip/CheckName
    
    #если имени нет - запрашиваем его
    state: AskName || modal = true
        random:
            a: Пожалуйста, назовите Ваше имя
            a: Как вас зовут?
    #имя совпало с переменной из списка - сохраняем имя, идем на Шаг2 заявки
        state: GetName
            q: * $Name *
            script: $client.name = $parseTree._Name.name;
            random:
                a: {{$client.name}}, приятно познакомиться!
                a: Приятно познакомиться, {{$client.name}}. Данные внесла в заявку
            go!: /Trip/Step2
    #не хочу знакомиться - извиняемся и идем на выход
        state: NoName
            q: $refuse
            q: ( * (не надо)/(не хочу)/(не буду) * )
            a: К сожалению, без имени я не могу принять заявку на тур
            go!: /Exit
    #выход из модального стейта при запросе погоды
        state: GoWeather
            q: $weather
            go!: /Weather/Begin 
    #другое непонятное слово - уточняем имя это или нет
        state: GetStrangeName
            event: noMatch
            script: $session.tempName = $request.query;
            random:
                a: {{$session.tempName}}! Какое необычное имя. Вы не ошиблись? Я могу вас так называть?
                a: Какое необычное имя! Давайте проверим: ваше имя {{$session.tempName}}?
            buttons:
                "Да"
                "Нет"
    #если имя - сохраняем его и идем на Шаг2 заявки
            state: Yes
                q: $yes
                script: $client.name = $session.tempName;
                a: {{$client.name}}, приятно познакомиться!
                go!: /Trip/Step2
    #если не имя - пробуем с начала
            state: No
                q: $no
                q: ( (* ошиб* *)/(* не може* *) )
                a: Попробуем еще раз.
                go!: /Trip/AskName

    #если телефон есть, идем его проверять, иначе - идем его спрашивать
    state: Step2
        script: $session.tripStep = "/Trip/Step2"
        if: $client.phone
            go!: /Trip/CheckPhone
        else:
            go!: /Trip/AskPhone
        
    #проверяем актуален ли телефон
    state: CheckPhone
        random:
            a: Ваш телефон {{$client.phone}}, верно?
            a: Это ваш номер {{$client.phone}}?
        buttons:
            "Да"
            "Нет"
    #если да - идем на Шаг3 заявки 
        state: Yes
            q: $yes
            a: Внесла в заявку
            go!: /Trip/Step3
    #если нет - идем спрашивать телефон
        state: No
            q: $no
            script: delete $client.phone
            go!: /Trip/AskPhone
    #если отказ - извиняемся и идем на выход
        state: Deny
            q: $refuse
            a: К сожалению, без номера телефона я не могу принять заявку
            go!: /Exit
    #если нет внятного ответа - возвращаемся к вопросу
        state: NoMatch || noContext = true 
            event: noMatch
            random:
                a: Пожалуйста, ответьте да или нет:
                a: Извините, непонятно. Ответьте да или нет:
            go!: /Trip/CheckPhone
           
    #если телефона нет - запрашиваем его
    state: AskPhone
        random:
            a: Назовите номер Вашего телефона
            a: Продиктуйте номер вашего телефона
    #имя совпало с переменной из списка - сохраняем имя, идем на Шаг2 заявки
        state: GetPhone
            q: * $phone *
            script: $client.phone = $parseTree._phone
            a: {{$client.phone}} внесла в заявку
            go!: /Trip/Step3
    #не хочу знакомиться - извиняемся и идем на выход
        state: NoPhone
            q: $refuse
            random:
                a: К сожалению, без номера телефона я не могу принять заявку
                a: Извините, оформить заявку без номера не получится
            go!: /Exit
    #иное - ругаемся и идем на начало
        state: NoMatch
            event: noMatch
            random:
                a: Непохоже на номер телефона. Давайте попробуем еще раз.
                a: Думаю, что это не номер телефона. Назовите номер ещё раз
            go!: /Trip/AskPhone
    #запрашиваем дату   
    state: Step3   
        script: $session.tripStep = "/Trip/Step3"
        random:
            a: Назовите дату начала поездки. Можно примерно
            a: Назовите примерную дата начала поездки
        buttons:
            "Еще не знаю"
    #введена дата - сохраняем ее
        state: Date
            q: * @duckling.date *
            script: $session.date = $parseTree.value;
            go!: /Trip/Step4
    #введено что-то иное - сохраняем это в другой переменной
        state: NoDate
            event: noMatch
            script: $session.noDate = $request.query;
            go!: /Trip/Step4
    
    #запрашиваем длительность поездки
    state: Step4 
        script: $session.tripStep = "/Trip/Step4"
        a: Выберите длительность поездки
        buttons:
            "до 7 дней"
            "7-13 дней"
            "14-20 дней"
            "21-29 дней"
            "Свыше месяца"
            "Еще не знаю"
    #подойдет любой ответ - записали его и идем на Шаг5 заявки
        state: NoMatch
            event: noMatch
            script: $session.duration = $request.query;
            go!: /Trip/Step5

    #запрашиваем количество участников поездки
    state: Step5 
        script: $session.tripStep = "/Trip/Step5"
        a: Сколько всего человек будет в поездке включая детей?
        buttons:
            "Еще не знаю"
        
    # если введено число - записали его
        state: Number
            q: * @duckling.number * || fromState = "/Trip/Step5", onlyThisState = true
            script: $session.people = $request.query;
            # если число больше 1 - пошли спросить про детей
            if: ($parseTree.value > 1)
                go!: /Trip/Step5/Children
            else: 
                go!: /Trip/Step6
    # спросили про детей 
        state: Children
            a: Сколько из них детей младше 14 лет?
    #подойдет любой ответ - записали его и идем на Шаг6 заявки
            state: NoMatch
                event: noMatch
                script: $session.children = $request.query;
                go!: /Trip/Step6
    # если любой другой ответ из корня Step5 - записали его как есть и идем на Шаг6 заявки
        state: Answer
            event: noMatch || fromState = "/Trip/Step5", onlyThisState = true
            script: $session.people = $request.query;
            go!: /Trip/Step6
            
    #запрашиваем бюджет поездки
    state: Step6 
        script: $session.tripStep = "/Trip/Step6"
        a: Какой примерно бюджет поездки из расчета на одного взрослого?
        buttons:
            "до $300"        
            "$300-$700"        
            "$700-$1500"        
            "$1500-$3000"
            "свыше $3000"
            "Еще не знаю"
    #подойдет любой ответ - записали его и идем на Шаг7 заявки
        state: NoMatch
            event: noMatch
            script: $session.budget = $request.query;
            go!: /Trip/Step7

    #запрашиваем бюджет поездки
    state: Step7 
        script: $session.tripStep = "/Trip/Step7"
        a: Какой хотите минимальный уровень звездности отеля?
        buttons:
            "не важно"        
            "3*"        
            "4*"        
            "5*"
            "Еще не знаю"
    #подойдет любой ответ - записали его и идем на Шаг8 заявки
        state: NoMatch
            event: noMatch
            script: $session.stars = $request.query;
            go!: /Trip/Step8

    #запрашиваем бюджет поездки
    state: Step8 || modal = true
        script: $session.tripStep = "/Trip/Step8"
        random:
            a: Что-то еще передать менеджеру? Может, какие-то пожелания?
            a: Озвучьте дополнительные пожелания к туру, если они есть
        buttons:
            "пожеланий нет" 
    #подойдет любой ответ - записали его и идем формировать почту
        state: NoMatch
            event: noMatch
            script: $session.comment = $request.query;
            go!: /SendMail/Mail

#============================================= Отправка формы на почту менеджеру =============================================#            
        
theme: /SendMail
    state: Mail
        random:
            a: Заявка сформирована, отправляю в компанию Just Tour...
            a: Подождите немного, отправляю заявку менеджерам Just tour
    #формируем и отправляем заявку
        script: $session.result = email ()
    #если результат ОК - сообщаем об этом и идем на четкий выход
        if: ($session.result.status == "OK")
            random:
                a: Заявка отправлена. Менеджер Just Tour свяжется с вами в ближайшее время
                a: Готово. Ожидайте звонка от менеджера Just tour
            go!: /End
    #если результат не ОК - тоже сообщаем об этом и идем на мягкий выход
        else:
            random:
                a: Упс. Ваша заявка не отправилась. Для подбора тура обратитесь в JustTour по телефону. Продиктовать номер?
                a: Что-то пошло не так, ваша заявка не отправлена. Вы можете самостоятельно связаться с менеджером. Готовы записать номер?
            #если согласие диктуем и спрашиваем надо ли повторить?
        
        state: Phone
            intent: /Да
            q: (* ~диктовать/~продиктовать *)
            a: Телефон Just Tour 8-812-000-0000. Повторить?
            #если надо повторить - идем на шаг назад
            state: Yes
                intent: /Да
                q: (* ~повторить/~диктовать/~продиктовать *)
                go!: /SendMail/Mail/Phone
        #во всех других случаях - идем на выход прощаться
        state: NoMatch
            event: noMatch
            go!: /End    
        
        
        
        
        