-- Der Beispieldatensatz: 60 Tickets, dazu Kommentare und Zeitbuchungen.
--
-- Bewusst ohne random(): jeder Teilnehmer bekommt exakt dieselben Zahlen, damit
-- jede Auswertung in der Anleitung nachpruefbar bleibt. Die Zeitpunkte haengen
-- an now(), damit die Tickets nie veraltet aussehen.
--
-- Die Moduln 7, 11, 8 und 6 sind teilerfremd gewaehlt - sonst haetten am Ende
-- alle kritischen Tickets dieselbe Firma.

WITH z AS (
    SELECT n,
           date_trunc('hour', now()) - ((61 - n) * interval '9 hours') AS gemeldet,
           ((n * 5) % 8) AS wer,
           (ARRAY['NIEDRIG','NORMAL','HOCH','KRITISCH',
                  'NORMAL','HOCH','KRITISCH'])[(n % 7) + 1] AS prio,
           (ARRAY['ZUGANG','NETZ','DRUCKER',
                  'HARDWARE','SOFTWARE','SONSTIGES'])[((n * 5) % 6) + 1] AS kat,
           (ARRAY['Mara Kruse','Jonas Feld','Sina Ahrens',
                  'Tobias Renk','Nils Baumann'])[(n % 5) + 1] AS wer_bearbeitet,
           CASE
               WHEN n % 11 < 5 THEN 'ERLEDIGT'
               WHEN n % 11 < 7 THEN 'IN_ARBEIT'
               WHEN n % 11 = 7 THEN 'WARTET'
               ELSE                 'NEU'
           END AS stand,
           (ARRAY[
               'Drucker im 2. OG zieht kein Papier ein',
               'VPN bricht nach wenigen Minuten ab',
               'Passwort zurueckgesetzt, Anmeldung klappt trotzdem nicht',
               'Excel startet nur noch im abgesicherten Modus',
               'Notebook laedt nicht mehr am Dock',
               'Scan an E-Mail landet im Spam',
               'WLAN im Besprechungsraum ohne Verbindung',
               'Zugriff auf das Projektlaufwerk verweigert',
               'Update haengt bei 47 Prozent',
               'Zweiter Monitor bleibt schwarz',
               'Telefonanlage nimmt keine Anrufe mehr an',
               'Rechnungsdruck bricht mit Fehlermeldung ab',
               'Postfach laeuft trotz Aufraeumen voll',
               'Kartenleser wird nicht mehr erkannt',
               'Serienbrief nummeriert falsch'
           ])[((n * 7) % 15) + 1] AS titel
    FROM generate_series(1, 60) AS n
)
INSERT INTO ticket (kennung, titel, beschreibung, firma, melder, kategorie,
                    prioritaet, status, bearbeiter, gemeldet_am, erledigt_am)
SELECT 'S-' || lpad(z.n::text, 4, '0'),
       z.titel,
       z.titel || '. Tritt seit heute frueh auf, ein Neustart hat nichts geaendert.',
       (ARRAY['Nordlicht Werften GmbH','Nordlicht Werften GmbH',
              'Stadtwerke Aurich','Stadtwerke Aurich',
              'Kontor Sued AG','Kontor Sued AG',
              'Praxis Dr. Hansen','Vos Logistik KG'])[z.wer + 1],
       (ARRAY['Anke Brehm','Bernd Kappel',
              'Carla Osei','Dirk Lammers',
              'Elif Yildiz','Frank Obermeier',
              'Greta Hansen','Hendrik Vos'])[z.wer + 1],
       z.kat,
       z.prio,
       z.stand,
       CASE WHEN z.stand <> 'NEU' THEN z.wer_bearbeitet END,
       z.gemeldet,
       CASE WHEN z.stand = 'ERLEDIGT'
            THEN z.gemeldet + (((z.n % 7) + 1) * interval '5 hours')
       END
FROM z;

-- Kommentare: der erste kommt vom Melder, die weiteren aus dem Support.
INSERT INTO kommentar (text, autor, geschrieben_am, ticket_id)
SELECT CASE c.k
           WHEN 1 THEN 'Bitte moeglichst heute noch, wir haengen daran fest.'
           WHEN 2 THEN 'Ich habe mir das angesehen und einen Verdacht.'
           WHEN 3 THEN 'Rueckfrage gestellt, warte auf Antwort.'
           ELSE        'Massnahme umgesetzt, bitte einmal gegenpruefen.'
       END,
       CASE WHEN c.k = 1 THEN t.melder ELSE coalesce(t.bearbeiter, 'Servicedesk') END,
       t.gemeldet_am + (c.k * interval '3 hours'),
       t.id
FROM ticket t
CROSS JOIN LATERAL generate_series(1, (t.id % 4) + 1) AS c(k);

-- Zeitbuchungen gibt es nur, wo jemand zustaendig ist.
INSERT INTO zeitbuchung (minuten, bearbeiter, ticket_id)
SELECT 15 * (((t.id + c.k) % 8) + 1),
       t.bearbeiter,
       t.id
FROM ticket t
CROSS JOIN LATERAL generate_series(1, (t.id % 5) + 1) AS c(k)
WHERE t.bearbeiter IS NOT NULL;

-- Die beiden festen Anmeldungen. Beide sind Bearbeiter und sehen dieselben
-- Tickets; nur Mara hat zusaetzlich die Rolle "assistent" und damit den
-- KI-Assistenten auf der Seite. An diesem einen Unterschied haengt die ganze
-- Vorfuehrung der Rollentrennung.
--
-- Passwoerter im Klartext, siehe die Anmerkung an der Tabelle in 01-schema.sql.
INSERT INTO benutzer (benutzername, passwort, rollen, anzeigename) VALUES
    ('mara',  'mara',  'bearbeiter,assistent', 'Mara Kruse'),
    ('jonas', 'jonas', 'bearbeiter',           'Jonas Feld');
