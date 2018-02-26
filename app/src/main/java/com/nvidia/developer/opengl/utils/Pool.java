////////////////////////////////////////////////////////////////////////////////
// Copyright 2017 mzhg
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License.  You may obtain a copy
// of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
// License for the specific language governing permissions and limitations
// under the License.
////////////////////////////////////////////////////////////////////////////////
package com.nvidia.developer.opengl.utils;

import java.util.ArrayList;
import java.util.List;

public class Pool<T> {
	
	private final List<T> freeObjects;
	private final PoolObjectFactory<T> factory;
	private final int maxSize;
	
	public Pool(PoolObjectFactory<T> factory, int maxSize) {
		this.factory = factory;
		this.maxSize = maxSize;
		this.freeObjects = new ArrayList<T>();
	}
	
	public T newObject(){
		return freeObjects.isEmpty() ? factory.createObject() : freeObjects.remove(freeObjects.size() - 1);
	}
	
	public void freeObject(T obj){
		if(freeObjects.size() < maxSize)
			freeObjects.add(obj);
	}

	public interface PoolObjectFactory<T>{
		public T createObject();
	}
}
