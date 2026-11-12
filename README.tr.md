[English](README.md) | **Türkçe**

# Employee Management API

Bir şirketin çalışan listesini yönetmek için Java ve Spring Boot ile geliştirilmiş bir RESTful web servisi.

Proje, Forage üzerindeki Hewlett Packard Enterprise (HPE) Yazılım Mühendisliği iş simülasyonu için yaptığım çalışmayla başladı. Simülasyon bir tasarım önerisi, bir `GET` ve bir `POST` uç noktası ile birim testlerini kapsıyor. Ben devam edip kendi önerimin geri kalanını da uyguladım: tam CRUD, girdi doğrulama, soft delete ve veritabanında kalıcılık. Nelerin eklendiği [Simülasyonun ötesinde](#simülasyonun-ötesinde) bölümünde tek tek listeleniyor.

## Özellikler

- HTTP üzerinden çalışan listeleme, getirme, ekleme, güncelleme ve silme
- Anlamlı durum kodlarıyla girdi doğrulama (`400`, `404`, `409`)
- Soft delete: silinen çalışanlar API'de görünmez ama veritabanında saklanır
- Spring Data JPA ile kalıcılık; veri yeniden başlatmada kaybolmaz
- Birim testleri ve HTTP düzeyinde testler

## Kullanılan teknolojiler

| Alan | Teknoloji |
|---|---|
| Dil | Java 17 |
| Framework | Spring Boot 4 (Spring Web MVC) |
| Kalıcılık | Spring Data JPA, H2 (dosya tabanlı) |
| Doğrulama | Jakarta Bean Validation |
| Test | JUnit 5, Mockito, MockMvc |
| Derleme | Gradle (wrapper dahil) |

## Başlarken

JDK 17 veya üstü gerekir. Başka bir şey kurmana gerek yok; Gradle wrapper Gradle'ı kendisi indirir, H2 da uygulamanın içinde çalışır.

```bash
git clone https://github.com/gokhanekici/employee-management-api.git
cd employee-management-api
./gradlew bootRun
```

Servis `http://localhost:8080` adresinde açılır. İlk çalıştırmada `./data/` altında yerel bir veritabanı oluşturur ve üç örnek çalışan yükler.

Testleri çalıştırmak için:

```bash
./gradlew test
```

## API

| Metot | Yol | Açıklama | Başarılı | Hatalar |
|---|---|---|---|---|
| `GET` | `/employees` | Aktif çalışanların tamamını listeler | `200` | – |
| `GET` | `/employees/{employee_id}` | Tek bir çalışanı getirir | `200` | `404` |
| `POST` | `/employees` | Çalışan ekler | `201` + `Location` başlığı | `400`, `409` |
| `PUT` | `/employees/{employee_id}` | Çalışanı günceller | `200` | `400`, `404` |
| `DELETE` | `/employees/{employee_id}` | Çalışanı soft delete ile siler | `204` | `404` |

Hata kodları:

- `400 Bad Request`: Bir alan eksik ya da boş, e-posta geçersiz, JSON bozuk veya (`PUT` için) URL'deki ID gövdedeki ID'den farklı.
- `404 Not Found`: Bu ID'ye sahip aktif bir çalışan yok.
- `409 Conflict`: `employee_id` zaten kullanılıyor. Soft delete ile silinmiş çalışanların ID'leri de buna dahil.

### Çalışan formatı

Beş alanın hepsi zorunludur.

```json
{
  "employee_id": "E1001",
  "first_name": "Ada",
  "last_name": "Lovelace",
  "email": "ada.lovelace@example.com",
  "title": "Software Engineer"
}
```

`GET /employees` listeyi bir nesnenin içinde döndürür:

```json
{
  "Employees": [
    {
      "employee_id": "E1001",
      "first_name": "Ada",
      "last_name": "Lovelace",
      "email": "ada.lovelace@example.com",
      "title": "Software Engineer"
    }
  ]
}
```

### Örnekler

Tüm çalışanları listele:

```bash
curl http://localhost:8080/employees
```

Çalışan ekle:

```bash
curl -i -X POST http://localhost:8080/employees \
  -H "Content-Type: application/json" \
  -d '{"employee_id":"E2001","first_name":"Linus","last_name":"Torvalds","email":"linus.torvalds@example.com","title":"Staff Engineer"}'
```

Çalışanı güncelle:

```bash
curl -i -X PUT http://localhost:8080/employees/E2001 \
  -H "Content-Type: application/json" \
  -d '{"employee_id":"E2001","first_name":"Linus","last_name":"Torvalds","email":"linus.torvalds@example.com","title":"Principal Engineer"}'
```

Çalışanı sil:

```bash
curl -i -X DELETE http://localhost:8080/employees/E2001
```

## Mimari

```
HTTP isteği
     │
EmployeeController     HTTP katmanı: yönlendirme, durum kodları, doğrulamayı tetikleme
     │
EmployeeManager        İş kuralları: benzersiz ID, soft delete
     │
EmployeeRepository     Veri erişimi (Spring Data JPA)
     │
H2 veritabanı          "employees" tablosu
```

| Sınıf | Görevi |
|---|---|
| `Employee` | JPA entity'si ve JSON modeli; doğrulama kuralları burada |
| `Employees` | `{"Employees": [...]}` cevabını üreten sarmalayıcı |
| `EmployeeController` | REST uç noktaları |
| `EmployeeManager` | İş mantığı |
| `EmployeeRepository` | Veritabanı sorguları |
| `DataSeeder` | Tablo boşsa örnek veriyi yükler |

## Tasarım kararları

**Soft delete.** `DELETE` kaydı silmez, pasif olarak işaretler. Yanlışlıkla yapılan silme işlemleri veritabanından geri alınabilir ve geçmiş korunur. Pasif çalışanlar bütün uç noktalar için "bulunamadı" gibi davranır.

**ID'ler yeniden kullanılmaz.** Soft delete ile silinmiş bir çalışanın ID'siyle yeni kayıt açmak `409` döndürür. ID yeniden kullanılsaydı eski kaydın geçmişi yeni bir kişiyle karışırdı.

**`PUT`, uyuşmayan ID'leri reddeder.** URL `E1001`, gövde `E2002` diyorsa istek belirsizdir. Servis hangi kaydın kastedildiğini tahmin etmek yerine `400` döndürür.

**Doğrulama modelin üzerinde.** Kurallar `Employee` sınıfındaki anotasyonlardır; `POST` ve `PUT` aynı kuralları tekrar yazmadan kullanır.

**İç durum alanı dışarıya açılmaz.** `active` alanı JSON'a dahil edilmez. İstemciler bu alanı okuyamaz ve değiştiremez; cevap formatı da istenen haliyle kalır.

**Katmanlı tasarım.** Bellekteki listeden veritabanına geçerken yalnızca veri katmanı değişti; controller'a ve onun birim testlerine dokunulmadı.

**Kolay kurulum için H2.** Tasarım önerisi, private cloud (HPE GreenLake) üzerinde PostgreSQL'i hedefliyor. Bu repo, herkes veritabanı kurmadan çalıştırabilsin diye dosya tabanlı H2 kullanıyor. Veri erişimi JPA üzerinden yapıldığı için PostgreSQL'e geçiş bir kod değişikliği değil, ayar değişikliğidir.

## Simülasyonun ötesinde

| Konu | Simülasyonun kapsamı | Bu proje |
|---|---|---|
| Uç noktalar | `GET` liste, `POST` | ID ile `GET`, `PUT` ve `DELETE` eklendi |
| Ekleme cevabı | Çalışanı eklemek | `201` ve yeni kaydı gösteren `Location` başlığı |
| Tekrarlanan ID | Kapsam dışı | `409` ile reddedilir |
| Girdi doğrulama | Kapsam dışı | Zorunlu alanlar ve e-posta biçimi, hata durumunda `400` |
| Silme | Yalnızca öneride | Soft delete olarak uygulandı |
| Veri saklama | Koda gömülü, bellekteki liste | Spring Data JPA ve dosya tabanlı veritabanı |
| JSON formatı | `"Employees"` altında `snake_case` alanlar | Jackson anotasyonlarıyla sağlanır, testlerle doğrulanır |
| Testler | Birim testleri | Mockito ile birim testleri ve MockMvc ile HTTP düzeyinde testler |
| Encapsulation | Getter ve setter'lar | Çalışan listesi dışarıya yalnızca değiştirilemez kopya olarak verilir |

## Testler

Test paketi üç katmandan oluşur:

- `EmployeeControllerTest`: Sahte (mock) `EmployeeManager` ile controller birim testleri (Mockito).
- `EmployeeManagerTest`: Bellekteki veritabanına karşı iş kuralları testleri.
- `EmployeeApiTest`: MockMvc üzerinden tam HTTP istekleri; yönlendirme, durum kodları, JSON alan adları ve doğrulama.

Testler ayrı, bellekte çalışan bir H2 veritabanı kullanır. Her test sonunda geri alındığı için testler birbirini etkilemez.

## Olası sonraki adımlar

- Kimlik doğrulama ve yetkilendirme; kayıtları yalnızca yetkili kullanıcılar değiştirebilsin
- Soft delete ile silinmiş çalışanları geri getiren ve saklama süresi dolunca kalıcı silen uç noktalar
- Geçersiz alanları adıyla bildiren hata cevapları
- PostgreSQL profili ve private cloud üzerine kurulum
- Büyük listeler için sayfalama

## Teşekkür ve kaynaklar

- Proje iskeleti, Spring'in [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service) rehberinden alınmıştır ([kaynak kod](https://github.com/spring-guides/gs-rest-service), Apache License 2.0).
- Başlangıç görevi, [Forage](https://www.theforage.com) üzerindeki Hewlett Packard Enterprise Yazılım Mühendisliği iş simülasyonundan gelmektedir.