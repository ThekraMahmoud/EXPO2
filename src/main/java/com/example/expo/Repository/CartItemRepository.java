شpackage com.example.expo.Repository;

import com.example.expo.Model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    java.util.Optional<CartItem> findByCart_IdAndMenuItem_Id(Integer cartId, Integer menuItemId);
}