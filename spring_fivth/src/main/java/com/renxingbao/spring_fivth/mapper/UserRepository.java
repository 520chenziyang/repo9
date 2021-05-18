package com.renxingbao.spring_fivth.mapper;

import com.renxingbao.spring_fivth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User,Integer> {
    @Query(value = "SELECT id ,user_name userName,user_pwd password from rxb_tb_user_mobile_user WHERE id =12345", nativeQuery = true)
  public List<User> findAll();

    @Query(value = "SELECT id ,user_name userName,user_pwd password from rxb_tb_user_mobile_user WHERE id =12346", nativeQuery = true)
    public List<User> kaqi();
}
