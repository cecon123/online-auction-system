# Chủ đề 3: OOP & Design Patterns (Bản Expert)

Tài liệu này đánh giá khả năng thiết kế phần mềm sạch (Clean Architecture) của bạn. Giảng viên sẽ ép bạn phải giải thích "Tại sao lại thiết kế như thế này mà không phải thế kia?".

---

## 1. Deep Dive: 4 Tính chất OOP & SOLID Connection

### 1.1 Tính Trừu tượng (Abstraction) & Đa hình (Polymorphism)
Mở file: `common/.../model/Item.java`
- `Item` là một **Abstract Class**. Tại sao không dùng Interface? 
  - *Đáp:* Vì Item chứa các TRẠNG THÁI (id, name, sellerId) chung của mọi loại hàng. Interface chỉ chứa hành vi (Behavior).
- Đa hình: `item.categoryDescription()` được override ở `Electronics`, `Art`. 
  - *Kết nối SOLID:* Đây là nguyên lý **Liskov Substitution**. Mọi nơi mong đợi kiểu `Item` đều có thể nhận vào `Art` mà không bị crash.

### 1.2 Tính Đóng gói (Encapsulation) & Data Integrity
Mở file: `common/.../model/Auction.java`
- Tại sao `currentPrice` là `private`?
- *Đáp:* Để bảo vệ tính nhất quán. Nếu để `public`, bất kỳ ai cũng có thể gán `auction.currentPrice = -100`. Thông qua Setter, em có thể ném ra `IllegalArgumentException`.

---

## 2. Design Patterns "Cấp cao" (Advanced Patterns)

### 2.1 Singleton: Vấn đề "Mảnh vỡ" (Double-checked locking)
Mở file: `Database.java`
Tại sao phải dùng từ khóa `volatile` cho biến `instance`?
- **Lý thuyết:** CPU có thể thực hiện lệnh không theo thứ tự (Instruction Reordering). Nó có thể cấp RAM cho object TRƯỚC khi chạy constructor. Một thread khác nhảy vào thấy instance đã có (nhưng chưa khởi tạo xong) sẽ bị lỗi. `volatile` ngăn chặn việc đổi thứ tự này.

### 2.2 Factory Method: ItemFactory
Mở file: `ItemFactory.java`
- **Tư duy thiết kế:** Nếu nhóm dùng `new Electronics()` trực tiếp trong `AuctionService`, thì `AuctionService` sẽ bị phụ thuộc (High Coupling) vào mọi subclass của Item. 
- **Giải pháp:** Factory che giấu việc tạo đối tượng. `AuctionService` chỉ cần biết `ItemFactory`.

---

## 3. Kho Câu hỏi Vấn đáp "Hacks/Tricky" (Bản Expert)

### Nhóm 1: OOP Chuyên sâu (10 câu)
1. **Q: Phân biệt "is-a" và "has-a" relationship?**
   - **A:** Is-a dùng Kế thừa (Inheritance). Has-a dùng Thành phần (Composition). Nhóm ưu tiên Has-a để giảm độ khớp.
2. **Q: Tại sao Java không hỗ trợ Đa kế thừa class (Multiple Inheritance)?**
   - **A:** Để tránh vấn đề "Diamond Problem" (Xung đột khi 2 lớp cha có cùng tên hàm). Java giải quyết qua đa kế thừa Interface.
3. **Q: Ý nghĩa của từ khóa `transient`? Nhóm có dùng không?**
   - **A:** Biến `transient` sẽ không bị Serialization (biến thành byte/JSON). Dùng cho các trường nhạy cảm như Password.
4. **Q: Interface mặc định (Default method) được dùng khi nào?**
   - **A:** Khi muốn thêm phương thức vào Interface mà không muốn làm hỏng các class cũ đã triển khai nó (Backward Compatibility).
5. **Q: Phân biệt `Shadowing` và `Overriding`?**
   - **A:** Shadowing xảy ra với biến (Variable), Java không hỗ trợ ghi đè biến. Overriding xảy ra với hàm.
6. **Q: Constructor có được kế thừa không?**
   - **A:** Không. Nhưng lớp con phải gọi `super()` ở dòng đầu tiên của constructor nó.
7. **Q: "Composition over Inheritance" nghĩa là gì?**
   - **A:** Là việc tạo ra các đối tượng phức tạp bằng cách lắp ghép các đối tượng nhỏ thay vì tạo ra một cây kế thừa quá sâu và cứng nhắc.
8. **Q: Tại sao `String` trong Java là Immutable (Bất biến)?**
   - **A:** Để an toàn đa luồng, tối ưu bộ nhớ (String Pool) và bảo mật (không bị thay đổi nội dung khi làm Key).
9. **Q: Lớp `Object` có những hàm gì quan trọng?**
   - **A:** `equals`, `hashCode`, `toString`, `clone`, `finalize`, `wait`, `notify`.
10. **Q: "Downcasting" là gì? Nó có nguy hiểm không?**
    - **A:** Là ép kiểu từ cha xuống con. Nguy hiểm vì có thể gây `ClassCastException`. Nên dùng `instanceof` kiểm tra trước.

### Nhóm 2: Design Patterns & SOLID (10 câu)
11. **Q: "Hollywood Principle" là gì? Nó liên quan gì đến Observer Pattern?**
    - **A:** "Don't call us, we'll call you". Server sẽ chủ động gọi Client khi có tin nhắn mới thay vì Client phải liên tục hỏi Server.
12. **Q: Làm sao để ngăn không cho người dùng Clone object Singleton của em?**
    - **A:** Ghi đè hàm `clone()` và ném ra `CloneNotSupportedException`.
13. **Q: Nguyên lý "Interface Segregation" (chữ I trong SOLID) áp dụng thế nào?**
    - **A:** Em không tạo ra một Interface "Siêu to khổng lồ" chứa 100 hàm. Em chia nhỏ thành `Authenticatable`, `BidderActions`, `SellerActions` để các lớp chỉ phải triển khai những gì nó thực sự cần.
14. **Q: Mẫu thiết kế "Builder" có thể dùng ở đâu trong dự án?**
    - **A:** Có thể dùng để tạo đối tượng `Auction` khi nó có quá nhiều tham số tùy chọn (startTime, endTime, reservePrice, increment...).
15. **Q: "Adapter Pattern" dùng để giải quyết bài toán gì?**
    - **A:** Khi em muốn kết nối 2 interface không tương thích (ví dụ: thư viện log của em dùng Logback nhưng thư viện ngoài dùng Log4j).
16. **Q: Phân biệt "Lazy Initialization" và "Eager Initialization" trong Singleton?**
    - **A:** Lazy: Chỉ tạo khi gọi `getInstance`. Eager: Tạo ngay khi class được load. Nhóm dùng Lazy để tiết kiệm tài nguyên.
17. **Q: Tại sao em lại dùng Interface cho lớp Service?**
    - **A:** Để hỗ trợ **Loose Coupling**. Nếu sau này em muốn thay đổi cách tính thầu, em tạo class `NewBidService` triển khai cùng Interface đó mà không cần sửa code ở Controller.
18. **Q: "Strategy Pattern" khác gì "State Pattern"?**
    - **A:** Strategy tập trung vào giải thuật (cách làm). State tập trung vào trạng thái (đối tượng đang ở đâu).
19. **Q: Làm sao để bảo mật Singleton khỏi đòn tấn công Serialization?**
    - **A:** Triển khai hàm `readResolve()` và trả về `instance`.
20. **Q: "Cohesion" (Độ kết dính) cao có lợi gì?**
    - **A:** Giúp code dễ hiểu, dễ sửa lỗi và dễ tái sử dụng vì một lớp chỉ tập trung vào một nhiệm vụ duy nhất.

### Nhóm 3: Những câu hỏi "Thách thức" (10 câu)
21. **Q: Giải thích "Flyweight Pattern"?**
22. **Q: Tại sao nhóm không dùng "Proxy Pattern" cho Database?**
    - **A:** Có thể dùng để triển khai cơ chế **Lazy Loading** (chỉ tải dữ liệu sản phẩm từ DB khi người dùng thực sự click vào xem).
23. **Q: Em hãy chỉ ra một điểm vi phạm SOLID trong code hiện tại (nếu có)?**
    - **A:** (Câu hỏi trung thực) Có thể một số Controller vẫn đang chứa hơi nhiều logic hiển thị lẫn logic chuyển hướng (vi phạm SRP).
24. **Q: "Open/Closed Principle" có làm cho code bị phức tạp quá mức không?**
    - **A:** Có, nếu áp dụng quá đà. Ta chỉ nên áp dụng cho những phần hay thay đổi.
25. **Q: Sự khác biệt giữa `Internal` (trong C#) và `Protected` (trong Java)?**
26. **Q: "Static Block" trong Java dùng để làm gì?**
    - **A:** Dùng để khởi tạo các biến tĩnh phức tạp ngay khi class được nạp vào bộ nhớ.
27. **Q: Tại sao không nên kế thừa từ các class như `ArrayList` hay `HashMap`?**
    - **A:** Vì chúng không được thiết kế để mở rộng. Nên dùng **Composition** (chứa nó bên trong).
28. **Q: Ý nghĩa của từ khóa `strictfp`?**
29. **Q: Một class có thể vừa là Abstract vừa là Final không?**
    - **A:** Không (Compile error). Vì Abstract yêu cầu phải được kế thừa, còn Final cấm kế thừa.
30. **Q: Làm sao để ép buộc mọi Item con đều phải cung cấp ảnh?**
    - **A:** Khai báo một abstract method `getRequiredImagePath()` trong lớp cha `Item`.

---

## 4. Giải mã Code (Code Walkthrough)

### File: `server/src/main/java/com/auction/server/dao/Database.java` (Singleton Expert)
```java
private static volatile Database instance;

public static Database getInstance() {
    Database result = instance; // Bước 1: Đọc vào biến cục bộ để tăng tốc
    if (result == null) {
        synchronized (Database.class) {
            result = instance;
            if (result == null) {
                instance = result = new Database(); // Bước 2: Khởi tạo an toàn
            }
        }
    }
    return result;
}
```
*Hỏi:* Tại sao lại có dòng `Database result = instance;`?
*Đáp:* Đây là một kỹ thuật tối ưu. Việc đọc biến `volatile` tốn kém hơn biến thường. Bằng cách copy vào biến cục bộ, ta giảm số lần truy cập vào bộ nhớ chính, giúp `getInstance` chạy nhanh hơn một chút.

### File: `common/src/main/java/com/auction/common/model/Item.java` (Abstraction)
```java
public abstract class Item extends Entity {
    // Thuộc tính chung
    private String name;
    private BigDecimal startingPrice;

    // Ép buộc lớp con phải triển khai
    public abstract String categoryDescription();
}
```
*Hỏi:* Tại sao `categoryDescription` không có thân hàm?
*Đáp:* Vì mỗi loại hàng có cách mô tả khác nhau (Xe thì có đời xe, Tranh thì có họa sĩ). Lớp cha không thể biết trước được nên để lớp con tự quyết định.
