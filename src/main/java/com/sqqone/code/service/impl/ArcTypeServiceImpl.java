package com.sqqone.code.service.impl;

import com.sqqone.code.entity.ArcType;
import com.sqqone.code.repository.ArcTypeRepository;
import com.sqqone.code.run.StartupRunner;
import com.sqqone.code.service.ArcTypeService;
import com.sqqone.code.util.Consts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author sqq
 * @version 1.0
 * @date 2020/6/11 13:08
 */
@Service
public class ArcTypeServiceImpl implements ArcTypeService {

    @Autowired
    private ArcTypeRepository arcTypeRepository;


    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    @Autowired
    private StartupRunner startupRunner;

    @Override
    public List<ArcType> list(Integer page, Integer pageSize, Sort.Direction direction, String... properties) {
        Page<ArcType> arcTypePage = arcTypeRepository.findAll(PageRequest.of(page-1,pageSize,direction,properties));
        return arcTypePage.getContent();
    }

    @Override
    public List listAll(Sort.Direction direction, String... properties) {
        if(redisTemplate.hasKey(Consts.ALL_ARC_TYPE_NAME)){
            return redisTemplate.opsForList().range(Consts.ALL_ARC_TYPE_NAME,0,-1);
        }else{
            List list = arcTypeRepository.findAll(Sort.by(direction,properties));
            if(list!=null&&list.size()>0){
                for(int i=0;i<list.size();i++){
                    redisTemplate.opsForList().rightPush(Consts.ALL_ARC_TYPE_NAME,list.get(i));
                }
            }
            return list;
        }
    }

    @Override
    public Long getCount() {
        return arcTypeRepository.count();
    }

    @Override
    public void save(ArcType arcType) {
        boolean flag = false;
        if(arcType.getArcTypeId()==null){
            flag = true;
        }
        arcTypeRepository.save(arcType);
        /*新增*/
        if(flag){
            redisTemplate.opsForList().rightPush(Consts.ALL_ARC_TYPE_NAME,arcType);

            /*修改*/
        }else{
            redisTemplate.delete(Consts.ALL_ARC_TYPE_NAME);

        }
        startupRunner.loadData();
    }

    @Override
    public void delete(Integer id) {
        arcTypeRepository.deleteById(id);
    }

    @Override
    public ArcType getById(Integer id) {
        return arcTypeRepository.getOne(id);
    }
}
