﻿
&НаСервере
Процедура ПриСозданииНаСервере(Отказ, СтандартнаяОбработка)
	
	Запрос = Новый Запрос;
	Запрос.Текст = 
	"ВЫБРАТЬ
    |  Документ.Ссылка,
    |  Документ.Дата
    |ИЗ
    |  Документ.РеализацияТоваровУслуг КАК Документ
    |ГДЕ
    |  Документ.Проведен
    |  И Документ.Дата МЕЖДУ &ДатаНачала И &ДатаОкончания
    |  И ТИПЗНАЧЕНИЯ(Документ.ЗаказКлиента) = ТИП(документ.ЗаказКлиента)
    |      И Документ.ЗаказКлиента <> ЗНАЧЕНИЕ(документ.ЗаказКлиента.ПустаяСсылка)";
КонецПроцедуры
